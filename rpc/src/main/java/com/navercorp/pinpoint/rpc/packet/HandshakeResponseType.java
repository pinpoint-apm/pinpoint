package com.nhn.pinpoint.rpc.packet;

public class HandshakeResponseType {
    
    public static class Success {
        public static final int CODE = 0;
        
        public static final HandshakeResponseCode SUCCESS = HandshakeResponseCode.SUCCESS;

        public static final HandshakeResponseCode SIMPLEX_COMMUNICATION = HandshakeResponseCode.SIMPLEX_COMMUNICATION;
        public static final HandshakeResponseCode DUPLEX_COMMUNICATION = HandshakeResponseCode.DUPLEX_COMMUNICATION;
    }

    public static class AlreadyKnown {
        public static final int CODE = 1;

        public static final HandshakeResponseCode ALREADY_KNOWN = HandshakeResponseCode.ALREADY_KNOWN;
        public static final HandshakeResponseCode ALREADY_SIMPLEX_COMMUNICATION = HandshakeResponseCode.ALREADY_SIMPLEX_COMMUNICATION;
        public static final HandshakeResponseCode ALREADY_DUPLEX_COMMUNICATION = HandshakeResponseCode.ALREADY_DUPLEX_COMMUNICATION;
    }

    public static class PropertyError {
        public static final int CODE = 2;

        public static final HandshakeResponseCode PROPERTY_ERROR = HandshakeResponseCode.PROPERTY_ERROR;
    }

    public static class ProtocolError {
        public static final int CODE = 3;

        public static final HandshakeResponseCode PROTOCOL_ERROR = HandshakeResponseCode.PROTOCOL_ERROR;
    }

    public static class Error {
        public static final int CODE = 4;

        public static final HandshakeResponseCode UNKNOWN_ERROR = HandshakeResponseCode.UNKNOWN_ERROR;
    }

}
