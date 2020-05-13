package main;

import java.util.*;

class Element implements Comparable<Element> {

  final Integer deadline;
  private final Integer value;

  Element(Integer value, Integer deadline) {
    this.value = value;
    this.deadline = deadline;
  }

  Integer getValue() {
    return value;
  }

  Integer getDeadline() {
    return deadline;
  }

  @Override
  public String toString() {
    return value + " - " + deadline;
  }

  @Override
  public int compareTo(Element o) {
    return value.compareTo(o.value);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Element) {
      Element temp = (Element) obj;
      return deadline.equals(temp.deadline)
              && value.equals(temp.value);
    }
    return false;
  }

}

class SimulationModule implements SimulationModuleBackbone {

  private final InputModule INPUT;
  private final List<Integer> REQUESTS;
  private List<Element> REQUESTS_D;
  private final int MAX;

  SimulationModule() {
    INPUT = new InputModule();
    REQUESTS = INPUT.getRequests();
    MAX = INPUT.getTrackAmount();
    System.out.print(Misc.getName("SIMULATION module"));
    System.out.printf(Misc.ALGO_LOG, "Initial:", REQUESTS.toString());
  }

  private void initDeadlineList() {
    REQUESTS_D = new ArrayList<>();
    List<Integer> temp = INPUT.getDeadlines(REQUESTS.size());
    for (int i = 0; i < REQUESTS.size(); i++)
      REQUESTS_D.add(new Element(REQUESTS.get(i), temp.get(i)));
  }

  @Override
  public List<Integer> runFCFS() {
    return packList("FCFS", new ArrayList<>(REQUESTS));
  }

  @Override
  public List<Integer> runSSTF() {
    List<Integer> base = new ArrayList<>(REQUESTS);
    List<Integer> res = new ArrayList<>();
    Integer curr = INPUT.getDiskHeadPos();
    for (int k = 0; k < REQUESTS.size(); k++) {
      curr = findNextClosest(curr, base, false);
      base.remove(curr);
      res.add(curr);
    }
    return packList("SSTF", res);
  }

  @SuppressWarnings("Duplicates")
  @Override
  public List<Integer> runSCAN() {
    List<Integer> base = new ArrayList<>(REQUESTS);
    Collections.sort(base);
    int s = base.size();
    // find the insertion place
    int pivot = INPUT.getDiskHeadPos();
    boolean descending = isDescending(base, pivot);
    int ind = findClosestInd(pivot, base, descending);
    if (descending) ind++;

    // work with the list
    Vector<Integer> sub1 = new Vector<>(base.subList(ind, s));
    Collections.reverse(base);
    Vector<Integer> sub2 = new Vector<>(base.subList(s - ind, s));

    // collect the result
    boolean check1 = sub2.contains(0) && descending;
    boolean check2 = sub1.contains(MAX) && !descending;
    List<Integer> res = new ArrayList<>(descending ? sub2 : sub1);
    if (!(check1 || check2) && !(sub1.isEmpty() || sub2.isEmpty()))
      res.add(null);
    res.addAll(descending ? sub1 : sub2);
    return packList("SCAN", res);
  }

  @SuppressWarnings("Duplicates")
  @Override
  public List<Integer> runCSCAN() {
    List<Integer> base = new ArrayList<>(REQUESTS);
    Collections.sort(base);
    int s = base.size();
    int pivot = INPUT.getDiskHeadPos();
    boolean descending = isDescending(base, pivot);
    int ind = findClosestInd(pivot, base, descending);
    if (descending) {
      ind++;
      Collections.reverse(base);
    }
    int finalInd = descending ? s - ind : ind;
    Vector<Integer> sub1 = new Vector<>(base.subList(finalInd, s));
    Vector<Integer> sub2 = new Vector<>(base.subList(0, finalInd));
    boolean check1 = sub1.contains(0) && descending
            || sub1.contains(MAX) && !descending;
    boolean check2 = sub2.contains(MAX) && descending
            || sub2.contains(0) && !descending;
    List<Integer> res = new ArrayList<>(sub1);
    if (!(sub1.isEmpty() || sub2.isEmpty())) {
      if (!check1) res.add(null);
      if (!check2) res.add(null);
    }
    res.addAll(sub2);
    return packList("C-SCAN", res);
  }

  @Override
  public Integer[][] runEDF() {
    if (REQUESTS_D == null)
      initDeadlineList();
    REQUESTS_D.sort(Comparator.comparing(o -> o.deadline));
    List<Element> base = new ArrayList<>(REQUESTS_D);
    packListD("EDF", base);
    return transformListD(base);
  }

  @Override
  public Integer[][] runFDSCAN() {
//    List<Element> res = new ArrayList<>();
    if (REQUESTS_D == null)
      initDeadlineList();
    /*int startPos = INPUT.getDiskHeadPos();
    Collections.sort(REQUESTS_D);
    int curr = findClosestInd(startPos, REQUESTS_D.stream().mapToInt(Element::getValue)
            .boxed().collect(Collectors.toList()), false);
    Element currEl = REQUESTS_D.get(curr);
    Element nextEl = findNextFeasible(REQUESTS_D);
    int next = REQUESTS_D.indexOf(nextEl);
    if (next < curr && currEl.getValue() > startPos) {
      curr--;
      res.addAll(REQUESTS_D.subList(next, curr));
    } else if (currEl.equals(nextEl)) {
      res.add(currEl);
      REQUESTS_D.remove(currEl);
    }*/

    return new Integer[][]{};
  }

  // Util methods
  private Integer findNextClosest(int el, List<Integer> list, boolean notEqual) {
    List<Integer> temp;
    if (notEqual) {
      temp = new ArrayList<>(list);
      temp.removeIf(e -> e == el);
      if (temp.isEmpty()) return el;
    } else temp = list;
    if (temp.size() > 1)
      temp.sort(Comparator.comparingInt(o -> Math.abs(o - el)));
    return temp.get(0);
  }

  private int findClosestInd(int el, List<Integer> list, boolean descending) {
    int[] deltas = list.stream().mapToInt(e -> Math.abs(e - el)).toArray();
    int temp;
    int res = 0;
    for (int j = 1; j < deltas.length; j++) {
      temp = deltas[j];
      boolean check1 = temp == deltas[res] && descending;
      boolean check2 = list.get(j).equals(list.get(res));
      if (temp < deltas[res] || check1 && check2)
        res = j;
      if (temp > deltas[j - 1])
        break;
    }
    return res;
  }

  private Element findNextFeasible(List<Element> list) {
    if (list == null || list.size() == 0)
      return null;
    List<Element> temp = new ArrayList<>(list);
    if (list.size() > 1)
      temp.sort(Comparator.comparing(o -> o.deadline));
    return temp.get(0);
  }

  private boolean isDescending(List<Integer> list, int arg) {
    int closestDifferent = findNextClosest(arg, list, true);
    return closestDifferent < arg;
  }

  private List<Integer> packList(String name, List<Integer> arg) {
    arg.add(0, INPUT.getDiskHeadPos());
    arg.add(MAX);
    System.out.printf(Misc.ALGO_LOG, name + ":", arg.toString());
    return arg;
  }

  private void packListD(String name, List<Element> arg) {
    arg.add(0, new Element(INPUT.getDiskHeadPos(), null));
    arg.add(new Element(MAX, null));
    System.out.printf(Misc.ALGO_LOG, name + ":", arg.toString());
  }

  private Integer[][] transformListD(List<Element> arg) {
    return new Integer[][]{
            arg.stream().map(Element::getValue).toArray(Integer[]::new),
            arg.stream().map(Element::getDeadline).toArray(Integer[]::new)
    };
  }

}