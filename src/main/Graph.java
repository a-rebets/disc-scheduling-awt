package main;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

class Graph extends JPanel implements GraphBackbone {

  // Main fields (these redraw with the component)
  private Graphics2D g2d;
  private Shape ARROW;
  private Point2D ARROW_INIT_POS;
  private Map<Integer, Integer> AXIS_LABELS;
  private int BASE_SIZE_REAL;
  private boolean CAN_PAINT = false;
  private boolean WAS_VISIBLE = true;
  boolean NEED_RECALCULATION = true;
  // (these are initialized only once)
  private boolean HAS_DEADLINES = false;
  private final int MAX_TRACK_NUM;
  private int MOVEMENTS = 0;
  private List<Point> POINTS;
  // Constants for the coordinate system
  private int AXIS_LENGTH_X;
  private int AXIS_LENGTH_Y;
  private int LOWER_BOUND_Y;
  private int LOWER_BOUND_X;
  private int UPPER_BOUND_X;
  private int UPPER_BOUND_Y;
  private int VERTICAL_STEP;

  ComponentAdapter RESIZE_ADAPTER = new ComponentAdapter() {
    @Override
    public void componentResized(ComponentEvent e) {
      if (e.getComponent().isValid())
        NEED_RECALCULATION = true;
    }
  };

  WindowAdapter WINDOW_ADAPTER = new WindowAdapter() {
    @Override
    public void windowOpened(WindowEvent e) {
      CAN_PAINT = true;
    }
  };

  ChangeListener SCROLL_LISTENER = e -> {
    Rectangle init = this.getParent().getBounds();
    int tempY = init.y + (init.height - 25);
    Rectangle part = new Rectangle(init.x, tempY, init.width, 25);
    Rectangle viewport = ((JViewport) e.getSource()).getViewRect();
    if (viewport.contains(part)) {
      if (!WAS_VISIBLE) {
        WAS_VISIBLE = true;
        repaint();
      }
    } else
      WAS_VISIBLE = false;
  };

  Graph(List<Integer> list) throws InstantiationException {
    setOpaque(false);
    if (list.size() < 3)
      throw new InstantiationException();
    int size = list.size() - 1;
    MAX_TRACK_NUM = list.get(size);
    list.remove(size);
    processBase(list);
  }

  Graph(List<Integer> list1, List<Integer> list2)
          throws InstantiationException {
    this(list1);
    HAS_DEADLINES = true;
    processDeadlines(list2);
    if (list1.size() != list2.size())
      throw new InstantiationException();
  }

  @Override
  public void processBase(List<Integer> list) {
    POINTS = new ArrayList<>();
    for (int n = 0; n < list.size(); n++) {
      POINTS.add(new Point(list.get(n)));
      if (n > 0)
        updateMovements(list, n);
    }
  }

  @Override
  public void processDeadlines(List<Integer> list) {
    list.remove(list.size() - 1);
    for (int n = 0; n < list.size(); n++)
      POINTS.get(n).setDeadline(list.get(n));
  }

  @Override
  public void processPoints() {
    int nonNullInd = 0;
    double coef = 1;
    boolean needCoefChange;
    for (int k = 0; k < POINTS.size(); k++) {
      Point point = POINTS.get(k);
      Point prevPoint = k > 0 ? POINTS.get(k - 1) : new Point();
      Integer prevVal = prevPoint.getValue();
      if (coef != 1) coef = 1 - coef;
      needCoefChange = false;

      if (point.getValue() != null) {
        double percent = point.getValue() * 1.0 / MAX_TRACK_NUM;
        point.setX(LOWER_BOUND_X + round(percent * AXIS_LENGTH_X));
        point.setY(VERTICAL_STEP * nonNullInd + LOWER_BOUND_Y);
        nonNullInd++;
      } else {
        Integer nextVal = POINTS.get(k + 1).getValue();
        if (prevVal == null) {
          point.setX(LOWER_BOUND_X + (UPPER_BOUND_X - prevPoint.getX()));
          needCoefChange = true;
        } else {
          boolean override = false;
          coef = 0.5;
          if (nextVal == null) {
            nextVal = POINTS.get(k + 2).getValue();
            coef = 0.25;
          } else if (prevVal == 0) {
            override = true;
            point.setX(UPPER_BOUND_X);
          } else if (prevVal == MAX_TRACK_NUM) {
            override = true;
            point.setX(LOWER_BOUND_X);
          }
          if (!override)
            point.setX(prevVal > nextVal ? UPPER_BOUND_X : LOWER_BOUND_X);
        }
        int calculated = round(VERTICAL_STEP * (nonNullInd - 1 + coef));
        point.setY(calculated + LOWER_BOUND_Y);
        if (needCoefChange) coef -= 0.25;
        point.hide();
      }
      if (k > 0) {
        double tanVal = VERTICAL_STEP * coef / (point.getX() - prevPoint.getX());
        double angle = Math.toDegrees(Math.atan(tanVal));
        point.setAngle(angle < 0 ? 180 + angle : angle);
      } else point.hide();
      if (needCoefChange) coef += 0.25;
      if (coef != 1 && point.getValue() != null) coef = 1;
    }
    generateServiceMap();
  }

  //  Processor which creates a map used to draw the axis labels
  private void generateServiceMap() {
    AXIS_LABELS = POINTS.stream()
            .filter(Misc.NON_NULL_POINT
                    .and(Misc.distinctByKey(Point::getValue)))
            .collect(Collectors.toMap(Point::getValue, Point::getX));
    AXIS_LABELS.putIfAbsent(0, LOWER_BOUND_X);
    AXIS_LABELS.putIfAbsent(MAX_TRACK_NUM, UPPER_BOUND_X);
  }

  //  ╔═════════════════════════ main part start ═════════════════════════╗

  @Override
  public void drawAxis() {
    g2d.drawLine(LOWER_BOUND_X, LOWER_BOUND_Y, UPPER_BOUND_X, LOWER_BOUND_Y);
    g2d.setFont(Misc.G_MAIN_FONT);
    for (Map.Entry<Integer, Integer> entry : AXIS_LABELS.entrySet())
      drawAxisLabel(entry.getKey(), entry.getValue());
  }

  @Override
  public void drawGrid() {
    float[] dash1 = {2f, 0f, 2f};
    BasicStroke bs1 = new BasicStroke(1, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_ROUND, 1.0f, dash1, 2f);
    g2d.setStroke(bs1);
    // horizontal grid
    int currY = LOWER_BOUND_Y;
    for (int i = 1; i < BASE_SIZE_REAL; i++) {
      currY += VERTICAL_STEP;
      g2d.drawLine(LOWER_BOUND_X, currY, UPPER_BOUND_X, currY);
    }
    // vertical grid
    int currX;
    g2d.setPaint(new Color(166, 167, 169));
    for (Map.Entry<Integer, Integer> mark : AXIS_LABELS.entrySet()) {
      currX = mark.getValue();
      if (mark.getKey() != 0 && mark.getKey() != MAX_TRACK_NUM)
        g2d.drawLine(currX, LOWER_BOUND_Y, currX, UPPER_BOUND_Y);
    }
    g2d.setPaint(Color.BLACK);
    g2d.setStroke(new BasicStroke());
  }

  @Override
  public void drawGraphBody() {
    g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
    g2d.setPaint(Misc.G_MAIN_COLOR);
    int[] x = POINTS.stream().mapToInt(Point::getX).toArray();
    int[] y = POINTS.stream().mapToInt(Point::getY).toArray();
    g2d.drawPolyline(x, y, POINTS.size());
    Point curr, prev;
    for (int z = 1; z < POINTS.size() + 1; z++) {
      curr = z < POINTS.size() ? POINTS.get(z) : new Point();
      prev = POINTS.get(z - 1);
      if (prev.isVisible())
        drawPoint(prev, z, curr.angle);
    }
    g2d.setPaint(Color.BLACK);
  }

  @Override
  public void doDrawing() {
    // Draw the background and reset the paint color
    g2d.setPaint(Misc.G_BG_COLOR);
    g2d.fillRect(LOWER_BOUND_X, LOWER_BOUND_Y, AXIS_LENGTH_X, AXIS_LENGTH_Y);
    g2d.setPaint(Color.BLACK);
    // Draw the grid
    drawGrid();
    // Draw the axis
    drawAxis();
    // Draw the graph
    ARROW = null;
    drawGraphBody();
  }

  //  Auxiliary graph drawing methods
  private void drawArrow(int x, int y, double ang) {
    if (ARROW == null) {
      FontRenderContext frc = g2d.getFontRenderContext();
      GlyphVector vector = g2d.getFont().createGlyphVector(frc, Misc.ARROW_SYMBOL);
      ARROW = vector.getGlyphOutline(0);
      ARROW_INIT_POS = vector.getGlyphPosition(0);
    }
    double ang0 = Math.toRadians(ang);
    double deltaH = ARROW.getBounds2D().getHeight() * 0.438;
    double deltaW = ARROW.getBounds2D().getWidth() * 0.1;
    double x0 = x - 3.5 * Math.cos(ang0) - deltaW;
    double y0 = y - 3.5 * Math.sin(ang0) + deltaH;
    AffineTransform at = AffineTransform.getTranslateInstance(x0, y0);
    at.rotate(-Math.PI + ang0, ARROW_INIT_POS.getX() + deltaW,
            ARROW_INIT_POS.getY() - deltaH);
    g2d.fill(at.createTransformedShape(ARROW));
  }

  private void drawAxisLabel(int val, int x) {
    String temp = val + "";
    g2d.drawLine(x, LOWER_BOUND_Y, x, LOWER_BOUND_Y - 6);
    float off = getTextOffset(temp)[0];
    float off1 = getAxisLabelShift(val);
    g2d.drawString(temp, x + off + off1, LOWER_BOUND_Y - 10);
  }

  private void drawPoint(Point point, int ind, Double nextAngle) {
    g2d.fillOval(point.x - 3, point.y - 3, 6, 6);
    drawArrow(point.x, point.y, point.angle);
    if (HAS_DEADLINES && ind > 1 && point.deadline != null) {
      String temp = String.format("(%d)", point.deadline);
      float[] shift = getDeadlineLabelShift(point.angle, nextAngle);
      float[] offset = getTextOffset(temp);
      shift[0] += point.x + offset[0];
      shift[1] += point.y;
      g2d.setPaint(Color.WHITE);
      g2d.fill3DRect(round(shift[0]), round(shift[1] - offset[1]) - 1,
              round(Math.abs(offset[0] * 2)), round(offset[1] * 3), true);
      g2d.setPaint(Misc.G_MAIN_COLOR);
      g2d.drawString(temp, shift[0], shift[1] + offset[1]);
    }
  }

  private void drawServiceInfo(String timeElapsed) {
    g2d.setFont(Misc.G_MAIN_FONT);
    String info = String.format(Misc.G_SERVICE_MSG, MOVEMENTS, "", timeElapsed);
    float[] infoOffset = getTextOffset(info);
    int infoX = round(UPPER_BOUND_X - 1 - infoOffset[0] * -2);
    int infoY = round(UPPER_BOUND_Y + infoOffset[1] * 2);
    infoX -= 10;
    int[] arrX = new int[]{infoX, infoX, UPPER_BOUND_X - 1, UPPER_BOUND_X - 1};
    infoY += 10;
    int[] arrY = new int[]{UPPER_BOUND_Y, infoY, infoY, UPPER_BOUND_Y};
    g2d.drawPolyline(arrX, arrY, 4);
    g2d.drawString(info, infoX + 5, infoY - 5);
  }

  //  Default component method for (re)drawing
  @Override
  public void paintComponent(Graphics g) {
    if (!CAN_PAINT) {
      repaint();
      return;
    }
    super.paintComponent(g);
    g2d = (Graphics2D) g;
    // Enable antialiasing and change rendering quality
    g2d.setRenderingHints(new RenderingHints(Misc.RH));
    // Count "real" points and update the vertical step accordingly
    if (NEED_RECALCULATION)
      initCoordinates(getWidth(), getHeight());
    BASE_SIZE_REAL = (int) POINTS.stream().filter(Misc.NON_NULL_POINT).count();
    VERTICAL_STEP = round(AXIS_LENGTH_Y * 1.0 / BASE_SIZE_REAL);

    long startTime = System.nanoTime();
    // Process the <Point> list
    processPoints();
    // Call the function drawing the content
    doDrawing();
    // Display the number of movements and elapsed drawing time
    drawServiceInfo(getTruncatedTime(System.nanoTime() - startTime));
  }

  //  ╚═════════════════════════ main part end ═════════════════════════╝

  //  Calculates margins between the axis labels
  private float getAxisLabelShift(int val) {
    List<Integer> values = new ArrayList<>(AXIS_LABELS.keySet());
    Collections.sort(values);
    int ind = values.indexOf(val);
    int leftDelta = ind > 0 ? values.get(ind) - values.get(ind - 1)
                            : Integer.MAX_VALUE;
    int rightDelta = ind < values.size() - 1
                     ? values.get(ind + 1) - values.get(ind)
                     : Integer.MAX_VALUE;
    return leftDelta <= 5 && rightDelta <= 5
           ? 0 : rightDelta <= 5 ? -6 : leftDelta <= 5 ? 6 : 0;
  }

  //  Calculates the position offset for deadline labels
  //  (relative to the graph vertices)
  private float[] getDeadlineLabelShift(Double alpha, Double beta) {
    float offset = 25;
    float deltaX, deltaY;
    double deltaAngle;

    if (beta != null) {
      deltaAngle = (180 - (alpha + beta)) / 2;
      if (Math.abs(alpha - beta) < 20)
        offset = 17;
    } else {
      beta = 90.0;
      deltaAngle = alpha < beta ? -alpha : 180 - alpha;
    }
    if (Math.abs(90 - Math.abs(deltaAngle)) < 30)
      offset = 17;
    double ang = Math.toRadians(Math.abs(deltaAngle));
    float coefY = deltaAngle < 0 ? -1 : 1;
    deltaX = offset * (float) Math.cos(ang)
            * (alpha < beta ? 1 : -1);
    deltaY = offset * (float) Math.sin(ang)
            * (alpha < beta ? -coefY : coefY);
    return new float[]{deltaX, deltaY};
  }

  //  Calculates a centering offset for any string on the plane
  private float[] getTextOffset(String text) {
    Font f = g2d.getFont();
    Rectangle2D bounds = f.getStringBounds(text, g2d.getFontRenderContext());
    FontMetrics metrics = g2d.getFontMetrics(f);
    return new float[]{
            -1 * ((float) bounds.getWidth() / 2),
            ((float) metrics.getAscent() - metrics.getDescent()) / 2,
    };
  }

  //  Util methods
  private String getTruncatedTime(long arg) {
    double temp = (double) arg / 1000000;
    return new DecimalFormat("#.###").format(temp);
  }

  private void initCoordinates(int w, int h) {
    LOWER_BOUND_X = round(w * 0.07);
    UPPER_BOUND_X = round(w * 0.93);
    LOWER_BOUND_Y = 20;
    UPPER_BOUND_Y = h - 21;
    AXIS_LENGTH_X = UPPER_BOUND_X - LOWER_BOUND_X;
    AXIS_LENGTH_Y = UPPER_BOUND_Y - LOWER_BOUND_Y;
    NEED_RECALCULATION = false;
    repaint();
  }

  private int round(double arg) {
    return Misc.round(arg);
  }

  private void updateMovements(List<Integer> list, int i) {
    Integer curr = list.get(i);
    Integer prev = list.get(i - 1);
    if (curr != null) {
      if (prev != null)
        MOVEMENTS += Math.abs(curr - prev);
    } else if (prev != null) {
      Integer next = list.get(i + 1);
      boolean needSwap = false;
      if (next == null) {
        next = list.get(i + 2);
        needSwap = true;
      } else if (prev == MAX_TRACK_NUM || prev == 0) {
        MOVEMENTS += MAX_TRACK_NUM;
        needSwap = true;
      }
      int bound = prev > next ? MAX_TRACK_NUM : 0;
      MOVEMENTS += Math.abs(bound - prev);
      if (needSwap)
        bound = bound == 0 ? MAX_TRACK_NUM : 0;
      MOVEMENTS += Math.abs(next - bound);
    } else
      MOVEMENTS += MAX_TRACK_NUM;
  }

}

// Inner class for a graph vertex
class Point {

  Integer x, y;
  Integer deadline;
  Double angle;
  private Integer value;
  private boolean visible = true;

  Point() {
  }

  Point(Integer arg) {
    value = arg;
  }

  boolean isVisible() {
    return visible;
  }

  Integer getValue() {
    return value;
  }

  int getX() {
    return x;
  }

  int getY() {
    return y;
  }

  void hide() {
    visible = false;
  }

  void setX(int arg) {
    x = arg;
  }

  void setY(int arg) {
    y = arg;
  }

  void setAngle(double arg) {
    angle = arg;
  }

  void setDeadline(Integer arg) {
    deadline = arg;
  }

}