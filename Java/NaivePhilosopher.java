package com.company;

import java.util.ArrayList;
import java.util.List;

public class NaivePhilosopher extends Philosopher {

    public NaivePhilosopher(int id, Fork leftFork, Fork rightFork) {
        super(id, leftFork, rightFork);
    }

    public NaivePhilosopher(int id, Fork leftFork, Fork rightFork, int eatingTime, int thinkingTime) {
        super(id, leftFork, rightFork, eatingTime, thinkingTime);
    }

    public void run() {
        while (true) {
            try {
                //thinking
                doSth(this.thinkingTime);

                startT = System.nanoTime();
                forks[0].get(id);
                forks[1].get(id);
                stopT = System.nanoTime();
                time.add(stopT - startT);

                eating();

                //eating
                doSth(this.eatingTime);

                forks[0].release();
                forks[1].release();
            }
            catch(InterruptedException ex) {
                return;
            }
        }
    }

}