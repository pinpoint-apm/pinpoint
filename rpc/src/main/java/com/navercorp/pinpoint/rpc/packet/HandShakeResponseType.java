package com.nhn.pinpoint.rpc.packet;

public class HandShakeResponseType {
    
    public static class Success {
        public static final int CODE = 0;
        
        public static final HandShakeResponseCode SUCCESS = HandShakeResponseCode.SUCCESS;

        public static final HandShakeResponseCode SIMPLEX_COMMUNICATION = HandShakeResponseCode.SIMPLEX_COMMUNICATION;
        public static final HandShakeResponseCode DUPLEX_COMMUNICATION = HandShakeResponseCode.DUPLEX_COMMUNICATION;
    }

    public static class AlreadyKnown {
        public static final int CODE = 1;

        public static final HandShakeResponseCode ALREADY_KNOWN = HandShakeResponseCode.ALREADY_KNOWN;
        public static final HandShakeResponseCode ALREADY_SIMPLEX_COMMUNICATION = HandShakeResponseCode.ALREADY_SIMPLEX_COMMUNICATION;
        public static final HandShakeResponseCode ALREADY_DUPLEX_COMMUNICATION = HandShakeResponseCode.ALREADY_DUPLEX_COMMUNICATION;
    }

    public static class PropertyError {
        public static final int CODE = 2;

        public static final HandShakeResponseCode PROPERTY_ERROR = HandShakeResponseCode.PROPERTY_ERROR;
    }

    public static class ProtocolError {
        public static final int CODE = 3;

        public static final HandShakeResponseCode PROTOCOL_ERROR = HandShakeResponseCode.PROTOCOL_ERROR;
    }

    public static class Error {
        public static final int CODE = 4;

        public static final HandShakeResponseCode UNKNOWN_ERROR = HandShakeResponseCode.UNKNOWN_ERROR;
    }

}
