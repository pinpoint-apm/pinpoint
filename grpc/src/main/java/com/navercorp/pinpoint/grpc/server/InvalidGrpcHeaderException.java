package com.navercorp.pinpoint.grpc.server;

import io.grpc.Metadata;

import java.util.Objects;

public class InvalidGrpcHeaderException extends RuntimeException {

    private final Metadata.Key<?> key;

    public InvalidGrpcHeaderException(Metadata.Key<?> key, String message) {
        super(message, null, false, false);
        this.key = Objects.requireNonNull(key, "key");
    }

    public Metadata.Key<?> getKey() {
        return key;
    }
}
