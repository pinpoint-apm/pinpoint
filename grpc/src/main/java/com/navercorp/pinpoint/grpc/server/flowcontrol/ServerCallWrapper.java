package com.navercorp.pinpoint.grpc.server.flowcontrol;

import io.grpc.Metadata;
import io.grpc.Status;

public interface ServerCallWrapper {
    String getAgentId();

    String getApplicationName();

    void request(int numMessages);

    void cancel(Status status, Metadata trailers);
}
