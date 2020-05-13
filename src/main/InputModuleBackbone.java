package main;

import java.util.List;

public interface InputModuleBackbone {

  /**
   * Returns a list of requests of a type corresponding
   * to the option chosen in the start menu.
   */
  List<Integer> getRequests();

  /*Standard getters for fields containing the number of tracks
   * on the disk and the initial position of the disk head
   * (they are used in the Simulation module).*/
  int getTrackAmount();

  int getDiskHeadPos();

  /**
   * Returns a list of randomly generated deadlines in the range
   * from 0 to 150 (list is size provided with the "lim" argument of function).
   */
  List<Integer> getDeadlines(int lim);

}