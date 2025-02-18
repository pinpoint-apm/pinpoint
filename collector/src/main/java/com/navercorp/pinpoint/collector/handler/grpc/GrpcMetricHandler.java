package com.navercorp.pinpoint.collector.handler.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.io.request.ServerRequest;
import org.springframework.stereotype.Component;

@Component
public interface GrpcMetricHandler {
    boolean accept(ServerRequest<GeneratedMessageV3> request);

    void handle(ServerRequest<GeneratedMessageV3> request);
}
