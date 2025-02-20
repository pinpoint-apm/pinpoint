package com.navercorp.pinpoint.grpc.protocol;


public class ProtocolVersionParser {

    public ProtocolVersion parse(String protocolVersion) {
        // legacy agent
        if (protocolVersion == null) {
            return ProtocolVersion.V1;
        }
        final int version = parseInt(protocolVersion, ProtocolVersion.V1.version());
        if (version == ProtocolVersion.V1.version()) {
            return ProtocolVersion.V1;
        }
        if (version == ProtocolVersion.V4.version()) {
            return ProtocolVersion.V4;
        }
        return ProtocolVersion.V1;
    }

    private int parseInt(String protocolVersion, int defaultValue) {
        try {
            return Integer.parseInt(protocolVersion);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

}
