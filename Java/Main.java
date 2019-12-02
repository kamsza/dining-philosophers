package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private enum Mode{ NAIVE, FAT_OR_HUNGRY, WITH_WAITER}
    private int philosophersNum = 5;
    private boolean testMode = false;
    private int maxThinkingTime = 100;
    private int maxEatingTime = 100;
    private Mode mode = Mode.FAT_OR_HUNGRY;

    private List<Fork> forks = new ArrayList<>();;
    private List<Philosopher> philosophers = new ArrayList<>();
    private Waiter waiter = null;


    public static void main(String[] args) throws InterruptedException {

        Main main = new Main();
        main.getData();
        main.createObjects();
        if(main.testMode) main.testOutput();
        else main.normalOutput();

    }


    private void getData() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Press ENTER to start default program or C + ENTER to enter data");
        String readString = sc.nextLine();

        while(readString != null) {
            if (readString.isEmpty()) return;
            else if(readString.equals("c") || readString.equals("C\n")) break;
            else {
                System.out.print("Press ENTER to start default program or C + ENTER to enter data");
                readString = sc.nextLine();
            }
        }

        System.out.print("Number of philosophers:  ");
        philosophersNum = sc.nextInt();

        System.out.print("Is it test measurement [t/n]:  ");
        sc.nextLine();
        String testModeStr = sc.nextLine();

        while(testModeStr != null) {
            if (testModeStr.equals("t") || testModeStr.equals("T")) { testMode = true; break; }
            else if(testModeStr.equals("n") || testModeStr.equals("N")) { testMode = false; break; }
            else {
                System.out.print("Is it test measurement [t/n]:  ");
                testModeStr = sc.nextLine();
            }
        }

        System.out.print("Max thinking time (in ms):  ");
        maxThinkingTime = sc.nextInt();

        System.out.print("Max eating time (in ms):  ");
        maxEatingTime = sc.nextInt();
    }


    private void createObjects() {
        for (int i = 0; i < philosophersNum; i++)
            forks.add(new Fork(i));


        switch(mode) {
            case NAIVE:
                for (int i = 0; i < philosophersNum; i++)
                    philosophers.add(new NaivePhilosopher(i, forks.get(i), forks.get((i + 1) % philosophersNum), maxEatingTime, maxThinkingTime));
                break;

            case FAT_OR_HUNGRY:
                for (int i = 0; i < philosophersNum; i++)
                    philosophers.add(new HungryOrFatPhilosopher(i, forks.get(i), forks.get((i + 1) % philosophersNum), maxEatingTime, maxThinkingTime));
                break;

            case WITH_WAITER:
                waiter = new Waiter(philosophersNum, forks);
                for (int i = 0; i < philosophersNum; i++)
                    philosophers.add(new PhilosopherWithWaiter(i, waiter, maxEatingTime, maxThinkingTime));
                break;
        }

        for (Philosopher p : philosophers)
            p.start();
    }

    private void testOutput() {

        Scanner sc = new Scanner(System.in);
        List<List<Long>> timeList = new ArrayList<>();
        int timeOfMeasurement;
        int shownTests;

        System.out.println("In this mode program will run in background, after given time number of cycles and time of waiting for forks will be written for each philosopher");
        System.out.print("Time of measurement (in ms):  ");
        timeOfMeasurement = sc.nextInt();
        System.out.print("Times to show:  ");
        shownTests = sc.nextInt();

        try { Thread.sleep(timeOfMeasurement); }
        catch (Exception ex) { ex.printStackTrace(); }

        for (Philosopher p : philosophers)
            p.interrupt();

        for (Philosopher p : philosophers)
            timeList.add(p.getTimeList());


        System.out.println("PHILOSOPHERS");
        for (int index = 0; index < timeList.size(); index++)
            System.out.print(String.format("%10s", index + 1));

        System.out.println("\nEATEN MEALS");
        for (Philosopher p : philosophers) {
            System.out.print(String.format("%10s", p.getEatenMealsNum()));
        }

        System.out.println("\nTIMES");
        for (int index = 0; index < shownTests; index++) {

            for (List<Long> time : timeList) {
                try {
                    System.out.print(String.format("%10s", time.get(index)));
                } catch (IndexOutOfBoundsException ex) {
                    System.out.print(String.format("%10s", " "));
                }
            }

            System.out.println();
        }
    }

    private void normalOutput() {
        System.out.println("In this mode program will run printing number of cycles each philosopher ended and actual owners of forks every few seconds");

        while (true) {
            List<String> philosopherTable = Philosopher.doPhilosopherTable(philosophers);
            List<String> forksTable = Fork.doForkTable(forks);

            System.out.println("=".repeat(98));
            for (int i = 0; i < philosophersNum + 1; i++)
                System.out.format("%40s%10s%2s%10s%40s\n", philosopherTable.get(i), " ", "||", " ", forksTable.get(i));
            System.out.println("=".repeat(98));
            System.out.println("\n".repeat(5));

            try { Thread.sleep(1000); }
            catch (Exception ex) { ex.printStackTrace(); }
        }
    }
}