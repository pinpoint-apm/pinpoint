package com.navercorp.pinpoint.collector.handler.grpc;

import com.navercorp.pinpoint.grpc.trace.PResult;

public final class PResults {
    private PResults() {
    }

    public static final PResult SUCCESS = PResult.newBuilder()
            .setSuccess(true)
            .build();

    public static final PResult INTERNAL_SERVER_ERROR = PResult.newBuilder()
            .setSuccess(false)
            .setMessage("Internal Server Error")
            .build();

    public static PResult serviceNotFound(String serviceName) {
        return PResult.newBuilder()
                .setSuccess(false)
                .setMessage("Service not found. serviceName=" + serviceName)
                .build();
    }

}
