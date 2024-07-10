package com.navercorp.pinpoint.collector.grpc.config;

import com.navercorp.pinpoint.collector.receiver.grpc.service.KeepAliveService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class GrpcKeepAliveScheduler {
    private final KeepAliveService keepAliveService;

    public GrpcKeepAliveScheduler(KeepAliveService keepAliveService) {
        this.keepAliveService = Objects.requireNonNull(keepAliveService, "keepAliveService");
    }

    @Scheduled(fixedRate = 300_000, scheduler = "grpcLifecycleScheduler")
    public void keepAliveServiceUpdate() {
        this.keepAliveService.updateState();
    }
}
