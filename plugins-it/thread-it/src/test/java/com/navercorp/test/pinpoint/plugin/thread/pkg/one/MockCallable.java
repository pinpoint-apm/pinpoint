package com.navercorp.test.pinpoint.plugin.thread.pkg.one;

import java.util.concurrent.Callable;

public class MockCallable implements Callable<String> {

    private String name;

    public MockCallable(String name) {
        this.name = name;
    }

    @Override
    public String call() {
        System.out.println("callable-----------------");
        return this.name;
    }
};
