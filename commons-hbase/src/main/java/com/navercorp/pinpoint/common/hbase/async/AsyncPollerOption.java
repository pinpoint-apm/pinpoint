package com.navercorp.pinpoint.common.hbase.async;

import com.navercorp.pinpoint.common.util.CpuUtils;

public class AsyncPollerOption {

    private int queueSize = 1000 * 100;

    private int writeBufferSize = 100;
    private int writeBufferPeriodicFlush = 100;
    private int parallelism = 0;
    private int cpuRatio = 1;
    private int minCpuCore = 2;

    private int connectionSize = 1;


    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getWriteBufferSize() {
        return writeBufferSize;
    }

    public void setWriteBufferSize(int writeBufferSize) {
        this.writeBufferSize = writeBufferSize;
    }

    public int getWriteBufferPeriodicFlush() {
        return writeBufferPeriodicFlush;
    }

    public void setWriteBufferPeriodicFlush(int writeBufferPeriodicFlush) {
        this.writeBufferPeriodicFlush = writeBufferPeriodicFlush;
    }

    public int getParallelism() {
        return cpu(parallelism, cpuRatio, minCpuCore);
    }

    int cpu(int parallelism, int cpuRatio, int minCpu) {
        if (parallelism <= 0) {
            final int cpuCount = getCpuCount();
            int cpu = Math.floorDiv(cpuCount, cpuRatio);
            return Math.max(cpu, minCpu);
        }
        return parallelism;
    }

    int getCpuCount() {
        return CpuUtils.cpuCount();
    }

    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }

    public int getCpuRatio() {
        return cpuRatio;
    }

    public void setCpuRatio(int cpuRatio) {
        this.cpuRatio = cpuRatio;
    }

    public int getMinCpuCore() {
        return minCpuCore;
    }

    public void setMinCpuCore(int minCpuCore) {
        this.minCpuCore = minCpuCore;
    }

    public int getConnectionSize() {
        return connectionSize;
    }

    public void setConnectionSize(int connectionSize) {
        this.connectionSize = connectionSize;
    }

    @Override
    public String toString() {
        return "AsyncPollerOption{" +
                "queueSize=" + queueSize +
                ", writeBufferSize=" + writeBufferSize +
                ", writeBufferPeriodicFlush=" + writeBufferPeriodicFlush +
                ", parallelism=" + parallelism +
                ", cpuRatio=" + cpuRatio +
                ", minCpuCore=" + minCpuCore +
                ", connectionSize=" + connectionSize +
                '}';
    }
}
