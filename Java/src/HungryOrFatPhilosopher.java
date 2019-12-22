package com.company;

import java.util.ArrayList;
import java.util.List;

public class HungryOrFatPhilosopher extends Philosopher {

    public HungryOrFatPhilosopher(int id, Fork leftFork, Fork rightFork) {
        super(id, leftFork, rightFork);
    }

    public HungryOrFatPhilosopher(int id, Fork leftFork, Fork rightFork, int eatingTime, int thinkingTime) {
        super(id, leftFork, rightFork, eatingTime, thinkingTime);
    }

    public void run() {
        while (true) {
            try {
                //thinking
                doSth(this.thinkingTime);

                startT = System.nanoTime();
                while (!forks[0].available() || !forks[1].available())
                    sleep(5);

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