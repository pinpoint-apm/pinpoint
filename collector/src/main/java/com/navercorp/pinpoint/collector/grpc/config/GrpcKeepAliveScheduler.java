package com.navercorp.pinpoint.collector.grpc.config;

import com.navercorp.pinpoint.collector.receiver.grpc.service.KeepAliveService;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class GrpcKeepAliveScheduler {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final ThrottledLogger throttledLogger = ThrottledLogger.getLogger(logger, 12);

    private final KeepAliveService keepAliveService;

    public GrpcKeepAliveScheduler(KeepAliveService keepAliveService) {
        this.keepAliveService = Objects.requireNonNull(keepAliveService, "keepAliveService");
    }

    @Scheduled(fixedRate = 300_000, scheduler = "grpcLifecycleScheduler")
    public void keepAliveServiceUpdate() {
        if (throttledLogger.isInfoEnabled()) {
            throttledLogger.info("keepAliveServiceUpdate started");
        }
        this.keepAliveService.updateState();
    }
}
