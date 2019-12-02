package com.company;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public abstract class Philosopher extends Thread {
    Random rand = new Random();

    public final int id;
    protected int eatenMealsNum = 0;
    protected Fork[] forks = new Fork[2];
    protected int thinkingTime = 50;
    protected int eatingTime = 100;

    protected static long startT, stopT;
    protected List<Long> time = new LinkedList<>();

    public Philosopher(int id, Fork leftFork, Fork rightFork) {
        this.id = id;
        this.forks[0] = leftFork;
        this.forks[1] = rightFork;
    }

    public Philosopher(int id, Fork leftFork, Fork rightFork, int eatingTime, int thinkingTime) {
        this.id = id;
        this.forks[0] = leftFork;
        this.forks[1] = rightFork;
        this.eatingTime = eatingTime;
        this.thinkingTime = thinkingTime;
    }

    public void run() {
    }

    protected void doSth(int howLong) throws InterruptedException {
        int randV = rand.nextInt(howLong);
        sleep(randV);
    }

    protected synchronized void eating() {
        eatenMealsNum++;
    }

    public synchronized int howManyTimesAte() {
        return eatenMealsNum;
    }

    public static List<String> doPhilosopherTable(List<Philosopher> philosophers) {
        List<String> returnString = new ArrayList<>();

        returnString.add(String.format("%17s%18s%5s", "PHILOSOPHER ID", "MEALS EATEN", " "));

        for(Philosopher p : philosophers) {
            returnString.add(String.format("%10s%10s%10s%10s", p.id, "|", p.howManyTimesAte(), " "));
        }


        return returnString;
    }

    public List<Long> getTimeList() { return time; }

    public int getEatenMealsNum() { return eatenMealsNum; }
}
