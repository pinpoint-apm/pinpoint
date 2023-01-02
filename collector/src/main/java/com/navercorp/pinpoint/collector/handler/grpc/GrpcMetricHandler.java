package com.navercorp.pinpoint.collector.handler.grpc;

import com.google.protobuf.GeneratedMessageV3;
import org.springframework.stereotype.Component;

@Component
public interface GrpcMetricHandler {
    boolean accept(GeneratedMessageV3 message);

    void handle(GeneratedMessageV3 message);
}
