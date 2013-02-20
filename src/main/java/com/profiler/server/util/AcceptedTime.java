package com.profiler.server.util;

import org.springframework.core.NamedThreadLocal;

/**
 *
 */
public class AcceptedTime {

    private static final ThreadLocal<Long> local = new NamedThreadLocal<Long>("AcceptedTime");

    public static void setAcceptedTime(long acceptedTime) {
        local.set(acceptedTime);
    }


    public static long getAcceptedTime() {
        Long acceptedTime = local.get();
        if (acceptedTime == null) {
            return System.currentTimeMillis();
        }
        return acceptedTime;
    }
}
