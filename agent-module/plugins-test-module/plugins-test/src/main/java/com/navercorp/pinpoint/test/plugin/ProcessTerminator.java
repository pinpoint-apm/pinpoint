package com.navercorp.pinpoint.test.plugin;

import org.tinylog.TaggedLogger;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ProcessTerminator {

    private final TaggedLogger logger;
    private final Process process;

    public ProcessTerminator(TaggedLogger logger, Process process) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.process = Objects.requireNonNull(process, "process");
    }

    public void destroy(long timeout, TimeUnit unit) {
        final boolean terminate = waitFor(process, timeout, unit);
        if (!terminate) {
            logger.warn("Process not terminated. Destroy process.");
            process.destroy();
        }

    }

    private boolean waitFor(Process process, long timeout, TimeUnit unit) {
        if (Thread.currentThread().isInterrupted()) {
            return false;
        }
        try {
            return process.waitFor(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn(e, "Process.waitFor() is interrupted");
            return false;
        }
    }
}
