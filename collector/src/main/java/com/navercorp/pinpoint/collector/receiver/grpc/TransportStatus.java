/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.grpc;

public class TransportStatus {

    public enum Code {
        OK(0, "Ok"),

        INVALID_ARGUMENT(1, "Client specified an invalid argument"),
        PERMISSION_DENIED(2, "The caller does not have permission to execute the specified operation"),
        UNAUTHENTICATED(3, "The request does not have valid authentication credentials for the operation"),
        INTERNAL_ERROR(4, "Internal errors");

        private final int value;
        private final String cause;

        private Code(int value, String cause) {
            this.value = value;
            this.cause = cause;
        }

        public String getCause() {
            return cause;
        }
    }

    public static final TransportStatus OK = new TransportStatus(Code.OK);
    public static final TransportStatus INVALID_ARGUMENT = new TransportStatus(Code.INVALID_ARGUMENT);
    public static final TransportStatus PERMISSION_DENIED = new TransportStatus(Code.PERMISSION_DENIED);
    public static final TransportStatus UNAUTHENTICATED = new TransportStatus(Code.UNAUTHENTICATED);
    public static final TransportStatus INTERNAL_ERROR = new TransportStatus(Code.INTERNAL_ERROR);

    private final Code code;

    private TransportStatus(Code code) {
        this.code = code;
    }

    public String getCause() {
        return code.getCause();
    }

    public String toString() {
        return code.getCause();
    }

    public boolean isOk() {
        return this.code == Code.OK;
    }
}