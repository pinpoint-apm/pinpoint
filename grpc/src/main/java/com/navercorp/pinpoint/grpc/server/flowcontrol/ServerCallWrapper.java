package com.navercorp.pinpoint.grpc.server.flowcontrol;

import io.grpc.Metadata;
import io.grpc.Status;

import java.net.SocketAddress;

public interface ServerCallWrapper {
    String getAgentId();

    String getApplicationName();

    void request(int numMessages);

    SocketAddress getRemoteAddr();

    void cancel(Status status, Metadata trailers);

    boolean isCancelled();
}
