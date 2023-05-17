package com.navercorp.pinpoint.realtime.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public class ScheduleUtil {

    public static ScheduledExecutorService makeScheduledExecutorService(String timerName) {
        final String name = "Schedule-" + timerName + "-Executor";
        final ThreadFactory threadFactory = new PrefixThreadFactory(name);
        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }

}