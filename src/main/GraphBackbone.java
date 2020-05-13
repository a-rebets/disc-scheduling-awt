package main;

import java.util.List;

interface GraphBackbone {

  /**
   * Processor for the initially received sequence.
   * Transforms it into a list of Point objects, which
   * contain a corresponding request value, XY coordinates,
   * incident angle(?), deadline(?) as fields. Executes at
   * the creation of the class instance.
   */
  void processBase(List<Integer> list);

  /**
   * Attaches deadlines to the created <Point> list
   */
  void processDeadlines(List<Integer> list);

  /**
   * Processor for the <Point> list - initializes all the
   * needed variables by working with the "value" field.
   * Executes at the resizing of the frame.
   */
  void processPoints();

  /**
   * Method for drawing the main axis. Includes scale
   * marks, the axis itself (line) and text labels.
   */
  void drawAxis();

  /**
   * Method for drawing the grid, which consists of
   * horizontal and vertical dashed lines (the last of
   * them are less contrasted).
   */
  void drawGrid();

  /**
   * Method for drawing the graph vertices. Plots points
   * (given their visibility) and connecting lines with
   * arrows. Attaches deadline labels if necessary.
   */
  void drawGraphBody();

  /**
   * Combines the graph drawing methods
   */
  void doDrawing();

}