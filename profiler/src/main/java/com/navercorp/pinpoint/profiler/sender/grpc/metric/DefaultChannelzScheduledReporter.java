package com.navercorp.pinpoint.profiler.sender.grpc.metric;

import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.grpc.ExecutorUtils;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class DefaultChannelzScheduledReporter implements ChannelzScheduledReporter {

    private static final long REPORT_INITIAL_DELAY_MS = 1000;

    private final ConcurrentMap<Long, ChannelzReporter> reporterMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduledExecutorService = newScheduledExecutorService();

    private final long reportPeriodMillis;

    public DefaultChannelzScheduledReporter(Duration reportPeriod) {
        Objects.requireNonNull(reportPeriod, "reportPeriod");
        this.reportPeriodMillis = reportPeriod.toMillis();
        if (this.reportPeriodMillis <= 0) {
            throw new IllegalArgumentException("Illegal report period: " + reportPeriod);
        }
    }
    private static ScheduledExecutorService newScheduledExecutorService() {
        String threadName = PinpointThreadFactory.DEFAULT_THREAD_NAME_PREFIX +
                DefaultChannelzScheduledReporter.class.getSimpleName();
        ThreadFactory threadFactory = new PinpointThreadFactory(threadName, true);
        return new ScheduledThreadPoolExecutor(1, threadFactory);
    }

    @Override
    public void registerRootChannel(final long id, final ChannelzReporter reporter) {
        Objects.requireNonNull(reporter, "reporter");

        final ChannelzReporter old = reporterMap.putIfAbsent(id, reporter);
        if (old != null) {
            return;
        }
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                reporter.reportRootChannel(id);
            }
        }, REPORT_INITIAL_DELAY_MS, this.reportPeriodMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        ExecutorUtils.shutdownExecutorService("ScheduledReporter", scheduledExecutorService);
    }

}
