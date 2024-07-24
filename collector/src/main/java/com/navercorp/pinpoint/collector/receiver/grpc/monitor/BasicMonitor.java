package com.navercorp.pinpoint.collector.receiver.grpc.monitor;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class BasicMonitor implements Monitor {
    private final Timer timer;

    public BasicMonitor(String name) {
        this.timer = new Timer(name, true);
    }

    public void register(Runnable job) {
        Objects.requireNonNull(job, "job");
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                job.run();
            }
        }, 60_000, 60_000);
    }


    @Override
    public void close() {
        this.timer.purge();
        this.timer.cancel();
    }
}