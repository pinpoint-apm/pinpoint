package com.navercorp.pinpoint.grpc.server.flowcontrol;

import java.util.concurrent.Future;

public interface ScheduledExecutor {
    Future<?> schedule(Runnable command);
}
