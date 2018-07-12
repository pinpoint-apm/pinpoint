/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.config;

import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class HttpStatusCodeErrors {
    private static final StatusCode ALL_STATUS_CODES = new StatusCode() {
        @Override
        public boolean isCode(int statusCode) {
            return 100 <= statusCode && statusCode <= 599;
        }
    };
    private static final List<String> DEFAULT_ERROR_CODES = Arrays.asList("5xx");

    private final StatusCode[] errors;

    public HttpStatusCodeErrors() {
        this(DEFAULT_ERROR_CODES);
    }

    public HttpStatusCodeErrors(final List<String> errorCodes) {
        this.errors = newErrorCode(errorCodes);
    }

    public boolean isHttpStatusCode(final int statusCode) {
        return ALL_STATUS_CODES.isCode(statusCode);
    }

    public boolean isErrorCode(final int statusCode) {
        for (StatusCode code : this.errors) {
            if (code.isCode(statusCode)) {
                return true;
            }
        }
        return false;
    }

    private StatusCode[] newErrorCode(List<String> errorCodes) {
        if (CollectionUtils.isEmpty(errorCodes)) {
            return new StatusCode[0];
        }

        List<StatusCode> statusCodeList = new ArrayList<StatusCode>();
        for (String errorCode : errorCodes) {
            if (errorCode.equalsIgnoreCase("5xx")) {
                statusCodeList.add(new ServerError());
            } else if (errorCode.equalsIgnoreCase("4xx")) {
                statusCodeList.add(new ClientError());
            } else if (errorCode.equalsIgnoreCase("3xx")) {
                statusCodeList.add(new Redirection());
            } else if (errorCode.equalsIgnoreCase("2xx")) {
                statusCodeList.add(new Success());
            } else if (errorCode.equalsIgnoreCase("1xx")) {
                statusCodeList.add(new Informational());
            } else {
                try {
                    final int statusCode = Integer.parseInt(errorCode);
                    statusCodeList.add(new DefaultStatusCode(statusCode));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return toArray(statusCodeList);
    }

    private <T> StatusCode[] toArray(List<StatusCode> list) {
        if (CollectionUtils.isEmpty(list)) {
            return new StatusCode[0];
        }
        return list.toArray(new StatusCode[0]);
    }


    private interface StatusCode {
        boolean isCode(int statusCode);
    }

    private static class DefaultStatusCode implements StatusCode {
        private final int statusCode;

        public DefaultStatusCode(final int statusCode) {
            this.statusCode = statusCode;
        }

        @Override
        public boolean isCode(int statusCode) {
            return this.statusCode == statusCode;
        }

        @Override
        public String toString() {
            return String.valueOf(statusCode);
        }
    }

    private static class Informational implements StatusCode {
        @Override
        public boolean isCode(int statusCode) {
            return 100 <= statusCode && statusCode <= 199;
        }

        @Override
        public String toString() {
            return "1xx";
        }
    }

    private static class Success implements StatusCode {
        @Override
        public boolean isCode(int statusCode) {
            return 200 <= statusCode && statusCode <= 299;
        }

        @Override
        public String toString() {
            return "2xx";
        }

    }

    private static class Redirection implements StatusCode {
        @Override
        public boolean isCode(int statusCode) {
            return 300 <= statusCode && statusCode <= 399;
        }

        @Override
        public String toString() {
            return "3xx";
        }

    }

    private static class ClientError implements StatusCode {
        @Override
        public boolean isCode(int statusCode) {
            return 400 <= statusCode && statusCode <= 499;
        }

        @Override
        public String toString() {
            return "4xx";
        }

    }

    private static class ServerError implements StatusCode {
        @Override
        public boolean isCode(int statusCode) {
            return 500 <= statusCode && statusCode <= 599;
        }

        @Override
        public String toString() {
            return "5xx";
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HttpStatusCodeErrors{");
        sb.append("errors=").append(Arrays.toString(errors));
        sb.append('}');
        return sb.toString();
    }
}