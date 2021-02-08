package com.navercorp.pinpoint.sdk.v1.concurrent.wrapper;

import com.navercorp.pinpoint.sdk.v1.concurrent.TraceCallable;
import com.navercorp.pinpoint.sdk.v1.concurrent.TraceRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

public class DefaultCommandWrapper implements CommandWrapper {

    public Runnable wrap(Runnable command) {
        if (command instanceof TraceRunnable) {
            return command;
        }
        return TraceRunnable.asyncEntry(command);
    }

    public <T> Callable<T> wrap(Callable<T> callable) {
        if (callable instanceof TraceCallable) {
            return callable;
        }
        return TraceCallable.asyncEntry(callable);
    }

    public <T> Collection<? extends Callable<T>> wrap(Collection<? extends Callable<T>> tasks) {
        final List<Callable<T>> wrapList = new ArrayList<>(tasks.size());
        for (Callable<T> task : tasks) {
            Callable<T> wrapTask = wrap(task);
            wrapList.add(wrapTask);
        }
        return wrapList;
    }
}
