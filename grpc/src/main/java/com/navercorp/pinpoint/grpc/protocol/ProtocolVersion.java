package com.navercorp.pinpoint.grpc.protocol;

public enum ProtocolVersion {
    V1(1_00),
    V4(4_00);

    private final int version;

    ProtocolVersion(int version) {
        this.version = version;
    }

    public int version() {
        return version;
    }
}
