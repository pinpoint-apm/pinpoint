package com.nhn.pinpoint.common.rpc.util;

/**
 *
 */
public class CpuUtils {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int WORKER_COUNT = CPU_COUNT * 2;

    public static int cpuCount() {
        return CPU_COUNT;
    }

    public static int workerCount() {
        return WORKER_COUNT;
    }
}
