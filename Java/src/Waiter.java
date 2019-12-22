package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;

public class Waiter {
    private final int N;
    private List<Fork> forks;
    private final Semaphore serveClient;
    Random rand = new Random();

    public Waiter(int N, List<Fork> forks) {
        this.N = N;
        this.forks = forks;
        this.serveClient = new Semaphore(N-1, false);
    }


    public void getForks(int philosopherId) throws InterruptedException {
        serveClient.acquire();

        forks.get(philosopherId).get(philosopherId);
        forks.get((philosopherId + 1)%5).get(philosopherId);
    }


    public void returnForks(int philosopherId) {
        forks.get(philosopherId).release();
        forks.get((philosopherId + 1)%5).release();
        serveClient.release();
    }


    public List<Fork> getForkList() {
        return forks;
    }
}
