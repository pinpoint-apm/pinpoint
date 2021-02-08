package com.navercorp.pinpoint.sdk.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public abstract class AsyncCallable {


    public Callable<Object> callable() {
        Callable<Object> task = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Object result = callableAsyncRun();
                if (result instanceof Future) {
                    return ((Future<?>) result).get();
                }
                return result;

            }
        };
        return task;
    }

    public abstract Object callableAsyncRun();
}


