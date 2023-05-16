package com.navercorp.pinpoint.grpc.server.flowcontrol;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class DirectScheduledExecutor implements ScheduledExecutor {

    @Override
    public Future<?> schedule(Runnable command) {
        FutureTask<?> futureTask = new FutureTask<>(command, null);
        futureTask.run();
        return futureTask;
    }
}
