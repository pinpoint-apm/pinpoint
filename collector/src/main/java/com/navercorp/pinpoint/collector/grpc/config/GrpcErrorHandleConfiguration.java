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
                if (status != null && status.getCode() == Status.CANCELLED.getCode()) {
                    // Ignore CANCELLED: client cancelled
                    return;
                }
            }
            logger.error("onErrorDropped", throwable);
        });
    }
}
