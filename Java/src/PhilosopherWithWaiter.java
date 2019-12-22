package com.company;

import java.util.ArrayList;
import java.util.List;

public class PhilosopherWithWaiter extends Philosopher {
    private Waiter waiter;

    public PhilosopherWithWaiter(int id, Waiter waiter) {
        super(id, null, null);
        this.waiter = waiter;
    }

    public PhilosopherWithWaiter(int id, Waiter waiter, int eatingTime, int thinkingTime) {
        super(id, null, null, eatingTime, thinkingTime);
        this.waiter = waiter;
    }


    public void run() {
        while(true) {
            try {
                //thinking
                doSth(this.thinkingTime);

                startT = System.nanoTime();
                waiter.getForks(id);
                stopT = System.nanoTime();
                time.add(stopT - startT);
                eating();

                //eating
                doSth(this.eatingTime);

                waiter.returnForks(id);
            }
            catch(Exception ex) {
                return;
            }
        }
    }

}
