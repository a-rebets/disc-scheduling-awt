package main;

import java.util.*;
import java.util.stream.Collectors;

public class InputModule implements InputModuleBackbone {

  private final int CHOICE;
  private final Scanner SCAN;
  private int DISK_HEAD_POS = 53;
  private Integer TRACK_AMOUNT = 200;
  private final List<Integer> PRESET = Arrays.asList(188, 5, 90, 123, 65, 169, 40, 7, 99, 0);
//  private final List<Integer> PRESET = Arrays.asList(1, 2, 3, 5, 8, 9, 11, 15, 16, 20, 21, 25, 34, 42);

  InputModule() {
    System.out.print(Misc.getName("INPUT module"));
    SCAN = new Scanner(System.in);
    System.out.printf(Misc.CHOICE_MSG1, Misc.CHOICE_MSG2);
    CHOICE = safeReadInteger(1, 3);
  }

  @Override
  public int getTrackAmount() {
    return TRACK_AMOUNT;
  }

  @Override
  public int getDiskHeadPos() {
    return DISK_HEAD_POS;
  }

  @Override
  public List<Integer> getRequests() {
    List<Integer> res = CHOICE == 1 ? PRESET
            : CHOICE == 2 ? getCustomRequests(true)
            : CHOICE == 3 ? getCustomRequests(false) : null;
    SCAN.close();
    return res;
  }

  @Override
  public List<Integer> getDeadlines(int lim) {
    return new Random().ints(lim, 0, 300)
            .boxed().collect(Collectors.toList());
  }

  private List<Integer> getCustomRequests(boolean random) {
    String msg = random ? "do you want to generate" : "are you going to provide";

    System.out.println(Misc.TRACK_AMOUNT_MSG);
    TRACK_AMOUNT = safeReadInteger(1, Integer.MAX_VALUE);
    if (TRACK_AMOUNT > 999)
      Misc.disableGUI(1);

    System.out.printf(Misc.INPUT_SIZE_MSG, msg);
    int n = safeReadInteger(1, 1000);
    if (n > 25 && Misc.GUI_ENABLED)
      Misc.disableGUI(2);

    System.out.printf(Misc.HEAD_POS_MSG, TRACK_AMOUNT);
    DISK_HEAD_POS = safeReadInteger(0, TRACK_AMOUNT);

    return random ? new Random().ints(n, 0, TRACK_AMOUNT)
            .boxed().collect(Collectors.toList()) : safeReadIntegers(n, TRACK_AMOUNT);
  }

  private int safeReadInteger(int min, int max) {
    String temp;
    while (true) {
      System.out.print("> ");
      temp = SCAN.nextLine();
      if (temp.matches(Misc.SINGLE_NUMBER_PATTERN)) {
        int res = Integer.parseInt(temp.trim());
        if (res >= min && res <= max)
          return res;
      }
      System.out.println(Misc.INPUT_ERR_MSG);
    }
  }

  private List<Integer> safeReadIntegers(int lim, int max) {
    String temp;
    String[] tempArr;
    List<Integer> res = new ArrayList<>();
    System.out.printf("You can start entering...%n%s%n", Misc.INPUT_HINT);
    while (lim > 0) {
      System.out.printf("[%d left] > ", lim);
      temp = SCAN.nextLine().trim();
      if (temp.isEmpty()) continue;
      tempArr = temp.split("\\s+");
      if (tempArr.length > 0 && !temp.matches(Misc.NOT_INT_OR_SPACE_PATTERN)) {
        List<Integer> toAdd = Arrays.stream(tempArr).mapToInt(Integer::parseInt)
                .filter(e -> e <= max && e >= 0).limit(lim).boxed().collect(Collectors.toList());
        res.addAll(toAdd);
        lim -= toAdd.size();
      } else
        System.out.println(Misc.INPUT_ERR_MSG);
    }
    return res;
  }

}