package com.navercorp.pinpoint.collector.handler.grpc.metric;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.handler.grpc.GrpcMetricHandler;
import com.navercorp.pinpoint.grpc.trace.PAgentUriStat;
import com.navercorp.pinpoint.io.request.ServerRequest;

public class DisableAgentUriGrpcMetricHandler implements GrpcMetricHandler {
    @Override
    public boolean accept(ServerRequest<GeneratedMessageV3> request) {
        GeneratedMessageV3 message = request.getData();
        return message instanceof PAgentUriStat;
    }

    @Override
    public void handle(ServerRequest<GeneratedMessageV3> request) {
    }

    @Override
    public String toString() {
        return "DisableAgentUriGrpcMetricHandler";
    }
}
