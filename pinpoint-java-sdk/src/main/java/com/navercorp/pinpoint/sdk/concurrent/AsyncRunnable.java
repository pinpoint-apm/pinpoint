package com.navercorp.pinpoint.sdk.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

public abstract class AsyncRunnable {
    public Runnable runnable() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                runnableAsyncRun();
            }
        };
        return runnable;
    }

    public abstract void runnableAsyncRun();

}
