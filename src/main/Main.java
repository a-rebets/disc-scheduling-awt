package main;

public class Main {

  public static void main(String[] args) {
    SimulationModule sim = new SimulationModule();
    OutputModule out = new OutputModule();

    out.add("FCFS", sim.runFCFS());

    out.add("SSTF", sim.runSSTF());

    out.add("SCAN", sim.runSCAN());

    out.add("C-SCAN", sim.runCSCAN());

    out.add("EDF", sim.runEDF());

    out.create();
  }

}