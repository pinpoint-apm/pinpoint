/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.rpc.packet;

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
