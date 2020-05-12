package com.navercorp.test.pinpoint.plugin.thread.pkg.two;

public class MockRunnable implements Runnable {
    public MockRunnable() {
    }

    @Override
    public void run() {
        System.out.println("runnable-----------------");
    }
};
