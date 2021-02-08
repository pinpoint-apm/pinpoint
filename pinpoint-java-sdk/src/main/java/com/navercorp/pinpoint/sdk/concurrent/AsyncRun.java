package com.navercorp.pinpoint.sdk.concurrent;

import java.util.concurrent.Callable;

public abstract class AsyncRun {

    public Runnable getRunnable() {
        AsyncRunnable asyncRunnable = new AsyncRunnable() {
            @Override
            public void runnableAsyncRun() {
                asyncRun();
            }
        };
        return asyncRunnable.runnable();
    }

    public Callable getCallable() {
        AsyncCallable asyncCallable = new AsyncCallable() {
            @Override
            public Object callableAsyncRun() {
                return asyncRun();
            }
        };
        return asyncCallable.callable();
    }


    public abstract Object asyncRun();
}
