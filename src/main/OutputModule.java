package main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class OutputModule implements OutputModuleBackbone,
        ActionListener, WindowStateListener {

  private final StringBuilder INIT_LOGS;
  private final List<Box> GRAPHS;
  private final List<String> NAMES;
  private final JPanel WRAPPER;
  private final BoxLayout WRAPPER_LAYOUT;
  private final Box FOOTER;
  private JComponent BASE;
  private Box NAV;
  private boolean IN_COMPARE_MODE = false;
  private int CURR_IND = 0;

  OutputModule() {
    GRAPHS = new ArrayList<>();
    NAMES = new ArrayList<>();
    INIT_LOGS = new StringBuilder();
    WRAPPER = new JPanel();
    WRAPPER_LAYOUT = new BoxLayout(WRAPPER, BoxLayout.PAGE_AXIS);
    WRAPPER.setLayout(WRAPPER_LAYOUT);
    WRAPPER.setOpaque(false);
    FOOTER = Box.createVerticalBox();
    FOOTER.setBorder(Misc.FOOTER_BORDER_1);
  }

  void add(String algorithmName, List<Integer> fromSimulation) {
    String log;
    if (!Misc.GUI_ENABLED) return;
    try {
      JLabel heading = getHeading(algorithmName + Misc.HEADING_TEXT);
      GRAPHS.add(getBody(new Graph(fromSimulation), heading));
      NAMES.add(algorithmName);
      log = String.format(Misc.PROCESS_MSG_OK, algorithmName);
    } catch (InstantiationException e) {
      log = String.format(Misc.PROCESS_MSG_ERR, algorithmName);
    }
    INIT_LOGS.append(log);
  }

  void add(String algorithmName, Integer[][] fromSimulation) {
    String log;
    if (!Misc.GUI_ENABLED) return;
    try {
      List<Integer> l1 = Misc.array2List(fromSimulation[0]);
      List<Integer> l2 = Misc.array2List(fromSimulation[1]);
      JLabel heading = getHeading(algorithmName + Misc.HEADING_TEXT);
      GRAPHS.add(getBody(new Graph(l1, l2), heading));
      NAMES.add(algorithmName);
      log = String.format(Misc.PROCESS_MSG_OK, algorithmName);
    } catch (InstantiationException e) {
      log = String.format(Misc.PROCESS_MSG_ERR, algorithmName);
    }
    INIT_LOGS.append(log);
  }

  private Box getBody(Graph g, JLabel h) {
    Box res = Box.createVerticalBox();
    res.add(h);
    res.add(g);
    return res;
  }

  private JLabel getHeading(String text) {
    JLabel res = new JLabel(text);
    res.setAlignmentX(Component.CENTER_ALIGNMENT);
    res.setBorder(Misc.HEADING_BORDER);
    res.setFont(Misc.G_MAIN_FONT.deriveFont(Font.BOLD, 20));
    return res;
  }


  @Override
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if (command.equals("e")) {
      IN_COMPARE_MODE = true;
      JFrame frame = (JFrame) BASE.getTopLevelAncestor();
      if (frame.getExtendedState() != Frame.MAXIMIZED_BOTH)
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
      else
        createComparison(frame, false);
    } else if (command.equals("0")) {
      disposeComparison();
    } else {
      boolean goingLeft = false;
      if (command.equals("p")) {
        goingLeft = true;
        CURR_IND = CURR_IND == 0 ? GRAPHS.size() - 1 : CURR_IND - 1;
      } else if (command.equals("n")) {
        CURR_IND = CURR_IND == GRAPHS.size() - 1 ? 0 : CURR_IND + 1;
      }
      System.out.printf(Misc.GUI_SWITCH_MSG, NAMES.get(CURR_IND),
              goingLeft ? "<-- " : "", goingLeft ? "" : " -->");
      WRAPPER.remove(0);
      WRAPPER.add(GRAPHS.get(CURR_IND));
      WRAPPER.validate();
      WRAPPER.repaint();
    }
  }

  @Override
  public void windowStateChanged(WindowEvent e) {
    if (e.getNewState() == Frame.MAXIMIZED_BOTH && IN_COMPARE_MODE)
      createComparison(e.getWindow(), true);
    else if (IN_COMPARE_MODE)
      disposeComparison();
  }

  private void createComparison(Window window, boolean wasResize) {
    System.out.printf(Misc.GUI_COMPARE_MSG, "Entered");
    JScrollPane scroll = new JScrollPane(BASE);
    scroll.getVerticalScrollBar().setUnitIncrement(15);
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    ((JFrame) window).setContentPane(scroll);
    window.validate();
    WRAPPER.remove(0);
    int viewWidth = scroll.getViewport().getExtentSize().width;
    transformWrapper(viewWidth);
    Graph temp;
    for (Box b : GRAPHS) {
      temp = (Graph) b.getComponent(1);
      if (!wasResize)
        temp.NEED_RECALCULATION = true;
      scroll.getViewport().addChangeListener(temp.SCROLL_LISTENER);
      WRAPPER.add(b);
    }
    FOOTER.remove(NAV);
    FOOTER.add(getExitButton(), 0);
    FOOTER.setBorder(Misc.FOOTER_BORDER_2);
    FOOTER.revalidate();
  }

  private void transformWrapper(int givenWidth) {
    int gap = 40;
    double horizontalK = (double) givenWidth / Misc.G_MIN_WIDTH;
    double dotPart = horizontalK % 1;
    int intPart = Misc.round(horizontalK - dotPart);
    double verticalK = Math.ceil((double) GRAPHS.size() / intPart);
    double finalWidth = (double) givenWidth / intPart;
    int wrapperHeight = Misc.round((finalWidth * 0.969 + gap) * verticalK - gap);
    GridLayout layout = new GridLayout(Misc.round(verticalK), intPart);
    layout.setVgap(gap);
    WRAPPER.setLayout(layout);
    WRAPPER.setPreferredSize(new Dimension(BASE.getWidth(), wrapperHeight));
  }

  private JButton getExitButton() {
    JButton exit = new JButton("EXIT COMPARISON");
    resetButton(exit);
    InputMap ip = exit.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ip.put(Misc.ESC_KEY_PRESSED, "pressed");
    ip.put(Misc.ESC_KEY_RELEASED, "released");
    exit.setActionCommand("0");
    exit.addActionListener(this);
    exit.setFont(Misc.G_MAIN_FONT.deriveFont(15f));
    exit.setAlignmentX(Component.CENTER_ALIGNMENT);
    return exit;
  }

  private void disposeComparison() {
    IN_COMPARE_MODE = false;
    JFrame frame = (JFrame) BASE.getTopLevelAncestor();
    WRAPPER.removeAll();
    WRAPPER.setLayout(WRAPPER_LAYOUT);
    WRAPPER.add(GRAPHS.get(CURR_IND));
    FOOTER.remove(0);
    FOOTER.add(NAV, 0);
    FOOTER.setBorder(Misc.FOOTER_BORDER_1);
    frame.setContentPane(BASE);
    frame.validate();
    frame.setSize(Misc.STANDARD_W_SIZE);
    frame.setLocationRelativeTo(null);
    System.out.printf(Misc.GUI_COMPARE_MSG, "Exited");
  }


  private void initUI(JFrame f) {
    f.setTitle("Disk scheduling");
    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    BASE = getContent();
    f.setContentPane(BASE);
    f.setSize(Misc.STANDARD_W_SIZE);
    f.setLocationRelativeTo(null);
    f.addWindowStateListener(this);
    Graph temp;
    for (Box b : GRAPHS) {
      temp = (Graph) b.getComponent(1);
      f.addWindowListener(temp.WINDOW_ADAPTER);
      f.addComponentListener(temp.RESIZE_ADAPTER);
    }
  }

  void create() {
    System.out.print(Misc.getName("OUTPUT module"));
    if (!Misc.GUI_ENABLED) {
      System.out.println(Misc.GUI_REJECT_MSG);
      return;
    }
    System.out.print(INIT_LOGS.toString());
    EventQueue.invokeLater(() -> {
      JFrame f = new JFrame();
      initUI(f);
      f.setVisible(true);
      int minW = Misc.G_MIN_WIDTH + (625 - BASE.getWidth());
      Dimension minD = new Dimension(minW, 760);
      f.setMinimumSize(minD);
      System.out.printf(Misc.GUI_INIT_MSG, NAMES.get(CURR_IND));
    });
  }

  private JComponent getContent() {
    Box content = Box.createVerticalBox();
    content.setOpaque(true);
    content.setBackground(Color.WHITE);
    try {
      NAV = getNav(Misc.getIconUrl("l"),
              Misc.getIconUrl("r"),
              Misc.getIconUrl("e"));
      FOOTER.add(NAV);
      FOOTER.add(Misc.getRigid(0, 10));
      FOOTER.add(getLogo(Misc.getLogoUrl()));
    } catch (IOException e) {
      e.printStackTrace();
    }
    WRAPPER.add(GRAPHS.get(CURR_IND));
    content.add(WRAPPER);
    content.add(FOOTER);
    return content;
  }

  // Create the component with control buttons
  private Box getNav(URL... u) throws IOException {
    Box buttonPanel = Box.createHorizontalBox();
    JButton prev = new JButton("Previous", new ImageIcon(ImageIO.read(u[0])));
    JButton next = new JButton("Next", new ImageIcon(ImageIO.read(u[1])));
    JButton expand = new JButton("Compare all", new ImageIcon(ImageIO.read(u[2])));
    processButtons(prev, next, expand);
    buttonPanel.add(prev);
    buttonPanel.add(Misc.getRigid(20, 0));
    buttonPanel.add(next);
    buttonPanel.add(Misc.getRigid(20, 0));
    buttonPanel.add(expand);
    buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    return buttonPanel;
  }

  private void processButtons(JButton... b) {
    b[1].setHorizontalTextPosition(AbstractButton.LEADING);
    b[2].setHorizontalTextPosition(AbstractButton.LEADING);
    b[2].setIconTextGap(12);
    resetButton(b[0]);
    InputMap ip0 = b[0].getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ip0.put(Misc.LEFT_KEY_PRESSED, "pressed");
    ip0.put(Misc.LEFT_KEY_RELEASED, "released");
    resetButton(b[1]);
    InputMap ip1 = b[1].getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ip1.put(Misc.RIGHT_KEY_PRESSED, "pressed");
    ip1.put(Misc.RIGHT_KEY_RELEASED, "released");
    Font f = Misc.G_MAIN_FONT.deriveFont(15f);
    b[2].setFont(f);
    f = f.deriveFont(Font.BOLD);
    b[0].setFont(f);
    b[1].setFont(f);
    for (int i = 0; i < b.length; i++) {
      b[i].setToolTipText(Misc.BUTTON_TOOLTIPS[i]);
      b[i].setActionCommand(Misc.BUTTON_COMMANDS[i]);
      if (GRAPHS.size() < 2)
        b[i].setEnabled(false);
      b[i].addActionListener(this);
    }
  }

  // Create a JLabel with the university logo
  private JLabel getLogo(URL url) throws IOException {
    BufferedImage img = ImageIO.read(url);
    int w = Misc.round(img.getWidth() * 0.3);
    int h = Misc.round(img.getHeight() * 0.3);
    BufferedImage scaledImg = new BufferedImage(w, h, img.getType());
    AffineTransform at = AffineTransform.getScaleInstance(0.3, 0.3);
    AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
    JLabel logo = new JLabel(new ImageIcon(ato.filter(img, scaledImg)));
    logo.setAlignmentX(Component.CENTER_ALIGNMENT);
    return logo;
  }


  // Util
  private void resetButton(JButton b) {
    InputMap ip = b.getInputMap(JComponent.WHEN_FOCUSED);
    ip.put(Misc.SPACE_PRESSED, "none");
    ip.put(Misc.SPACE_RELEASED, "none");
  }

}