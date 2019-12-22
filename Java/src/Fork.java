package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Fork {
    public final int id;
    private final Semaphore available = new Semaphore(1);

    public Fork(int id) { this.id = id; }

    private int whoHasMe = -1;


    public void get(int philosopherId) throws InterruptedException{
        available.acquire();
        whoHasMe = philosopherId;
    }


    public void release() {
        available.release();
        whoHasMe = -1;
    }


    public boolean available() {
        return available.availablePermits() == 1;
    }


    public String whoHasMe() {
        return whoHasMe == -1 ? "-" : String.valueOf(whoHasMe);
    }


    public static List<String> doForkTable(List<Fork> forks) {
        List<String> returnString = new ArrayList<>();

        returnString.add(String.format("%13s%20s%7s", "FORK ID", "WHO HAS ME", " "));

        for(Fork f : forks) {
            returnString.add(String.format("%10s%10s%10s%10s", f.id, "|", f.whoHasMe(), " "));
        }

        return returnString;
    }
}
