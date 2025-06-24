package com.navercorp.pinpoint.collector.util;

import io.grpc.Status;

public class ErrorStatus {
    public static final Status InternalBadRequest = Status.INTERNAL.withDescription("Bad Request");
}
