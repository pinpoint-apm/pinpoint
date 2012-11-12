package com.profiler.context;

import com.profiler.sender.DataSender;

import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class GlobalCallTrace<T> {
    private static AtomicInteger timerId = new AtomicInteger(0);

    private ConcurrentMap<Integer, T> trace = new ConcurrentHashMap<Integer, T>(32);
    private AtomicInteger idGenerator = new AtomicInteger(0);
    private DataSender dataSender;

    private Timer timer = new Timer("GlobalCallTrace-Timer-" + timerId.getAndIncrement(), true);

    public int registerTraceObject(T target) {
        int id = idGenerator.getAndIncrement();
        trace.put(id, target);
        return id;
    }

    public T removeTraceObject(int id) {
        return trace.get(id);
    }

    public void setDataSender(DataSender dataSender) {
        this.dataSender = dataSender;
    }
}
