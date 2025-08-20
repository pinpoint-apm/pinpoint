package com.navercorp.pinpoint.collector.grpc.config;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

@Configuration
public class GrpcErrorHandleConfiguration {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public GrpcErrorHandleConfiguration() {
        Hooks.onErrorDropped(throwable -> {
            if (throwable instanceof StatusRuntimeException) {
                Status status = ((StatusRuntimeException) throwable).getStatus();
                // Ignore CANCELLED: client cancelled
                if (status.getCode() != Status.CANCELLED.getCode()) {
                    logger.error("onErrorDropped", throwable);
                }
            }
        });
    }
}
