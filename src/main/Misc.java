package main;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.security.Key;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("SpellCheckingInspection")
class Misc {

  private static Border getBorder(int h, int v) {
    return BorderFactory.createEmptyBorder(v, h, v, h);
  }

  static boolean GUI_ENABLED = true;
  static String GUI_REJECT_MSG = "A graphical interface for the sequence will not be provided\nbecause";
  private static final String LOGO_PATH = "/resources/poli-logo.jpeg";
  private static final String DELIMITER = ">< >< >< >< >< >< >< >< >< ><";
  static final String HEADING_TEXT = " scheduling algrorithm performance";
  static final int G_MIN_WIDTH = 550;
  static final Dimension STANDARD_W_SIZE = new Dimension(625, 815);
  static final Color G_MAIN_COLOR = new Color(41, 121, 255);
  static final Color G_BG_COLOR = new Color(209, 210, 212);
  static final Font G_MAIN_FONT = new Font("Arial", Font.PLAIN, 14);
  static final String ARROW_SYMBOL = Character.toString((char) 0x25C0);
  static final String G_SERVICE_MSG = "Total movements: %d%6sDrawing time: %s ms";
  static final String ALGO_LOG = "%1$-10s%2$s%n";
  static final String CHOICE_MSG1 = "Which input type would You like to use?%n%s%n";
  static final String CHOICE_MSG2 = "1 - Preset  2 - Random  3 - Custom";
  static final String TRACK_AMOUNT_MSG = "Enter the max number of tracks on the disk:";
  static final String INPUT_SIZE_MSG = "How many requests %s?%n";
  static final String HEAD_POS_MSG = "Specify the initial position of the disk head (up to %d):%n";
  static final String SINGLE_NUMBER_PATTERN = "\\s*-*\\d+\\s*";
  static final String NOT_INT_OR_SPACE_PATTERN = ".*(?!\\d|-)\\S.*";
  static final String INPUT_HINT = "(multiple requests/line allowed, use spaces or tabs)";
  static final String INPUT_ERR_MSG = "Please, enter valid data!";
  static final String PROCESS_MSG_OK = "Successfully processed the values for %s..%n";
  static final String PROCESS_MSG_ERR = "Could not process the values for %s!%n";
  static final String GUI_INIT_MSG = "%nGUI initialized, displaying %s%n";
  static final String GUI_SWITCH_MSG = "%2$sSwitched to %1$s%3$s%n";
  static final String GUI_COMPARE_MSG = "<<%s compare mode>>%n";
  static final Predicate<Point> NON_NULL_POINT = e -> e.getValue() != null;
  static final Map<RenderingHints.Key, Object> RH;
  static final Border FOOTER_BORDER_1 = getBorder(15, 15);
  static final Border FOOTER_BORDER_2 = getBorder(30, 30);
  static final Border HEADING_BORDER = getBorder(0, 10);
  static final String[] BUTTON_TOOLTIPS = new String[]{
          "View the previous scheduling algorithm.",
          "View the next scheduling algorithm.",
          "Compare all algorithms side-by-side."
  };
  static final String[] BUTTON_COMMANDS = new String[]{"p", "n", "e"};
  static final KeyStroke SPACE_PRESSED = KeyStroke.getKeyStroke("pressed SPACE");
  static final KeyStroke SPACE_RELEASED = KeyStroke.getKeyStroke("released SPACE");
  static final KeyStroke LEFT_KEY_PRESSED = KeyStroke.getKeyStroke("pressed LEFT");
  static final KeyStroke RIGHT_KEY_PRESSED = KeyStroke.getKeyStroke("pressed RIGHT");
  static final KeyStroke LEFT_KEY_RELEASED = KeyStroke.getKeyStroke("released LEFT");
  static final KeyStroke RIGHT_KEY_RELEASED = KeyStroke.getKeyStroke("released RIGHT");
  static final KeyStroke ESC_KEY_PRESSED = KeyStroke.getKeyStroke("pressed ESCAPE");
  static final KeyStroke ESC_KEY_RELEASED = KeyStroke.getKeyStroke("released ESCAPE");

  static {
    RH = new HashMap<>();
    RH.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    RH.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  }

  static void disableGUI(int cause) {
    GUI_REJECT_MSG += (cause == 1) ? " the maximum number of tracks is too high (> 999)."
                      : " the total amount of requests is too large (> 25).";
    GUI_ENABLED = false;
  }

  static URL getLogoUrl() { return Misc.class.getResource(LOGO_PATH); }

  static URL getIconUrl(String dir) {
    String name = (dir.equals("l")) ? "left-arrow.png"
                                    : (dir.equals("r")) ? "right-arrow.png"
                                                        : "expand.png";
    return Misc.class.getResource("/resources/" + name);
  }

  static String getName(String arg) {
    String pattern = "%n%1$s %2$s %1$s%n%n";
    return String.format(pattern, DELIMITER, arg);
  }

  static Component getRigid(int w, int h) { return Box.createRigidArea(new Dimension(w, h)); }

  static List<Integer> array2List(Integer[] arr) { return new ArrayList<>(Arrays.asList(arr)); }

  static Predicate<Point> distinctByKey(Function<Point, Integer> f) {
    Map<Object, Boolean> seen = new ConcurrentHashMap<>();
    return t -> seen.putIfAbsent(f.apply(t), true) == null;
  }

  static int round(double arg) { return (int) Math.round(arg); }

}