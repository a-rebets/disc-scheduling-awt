package main;

import java.util.List;

interface SimulationModuleBackbone {

  // All the algorithms

  /**
   * Serves all requests one-by-one in the order
   * they appeared in the queue (without substitutions).
   */
  List<Integer> runFCFS();

  /**
   * Searches for the closest request to the current position,
   * serves it, repeats this cycle until the end (sorting by delta).
   */
  List<Integer> runSSTF();

  /**
   * Moving disk head serves all requests on its way from one
   * side of the cylinder to another (combination of ascending /
   * descending sort).
   */
  List<Integer> runSCAN();

  /**
   * Almost the same as SCAN, though when the disk head reaches the
   * end of the cylinder - its jumps back to the opposite side.
   */
  List<Integer> runCSCAN();

  /**
   * Requests are served consecutively, a new one is picked such
   * that it has the closest deadline among the others.
   */
  Integer[][] runEDF();

  /**
   * "Feasible Deadline SCAN" - find a request with the earliest
   * deadline and start "scanning" in its direction (moving the disk
   * head and serving all requests along the way), then repeat the cycle.
   */
  Integer[][] runFDSCAN();

}