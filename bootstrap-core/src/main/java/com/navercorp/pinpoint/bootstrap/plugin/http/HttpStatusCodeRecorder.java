/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.http;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class HttpStatusCodeRecorder {
    private static final StatusCode ALL_STATUS_CODES = new StatusCode() {
        @Override
        public boolean isCode(int statusCode) {
            return 100 <= statusCode && statusCode <= 599;
        }
    };

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final StatusCode[] errors;

    public HttpStatusCodeRecorder(final List<String> errorCodes) {
        this.errors = newErrorCode(errorCodes);

        if (isDebug) {
            logger.debug("Initialized HTTP status code of errors={}", this.errors);
        }
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
        return list.toArray(new StatusCode[list.size()]);
    }

    public void record(final SpanRecorder spanRecorder, final int statusCode) {
        if (spanRecorder == null) {
            // ignored
            return;
        }

        if (!ALL_STATUS_CODES.isCode(statusCode)) {
            // out of range status codes.
            return;
        }

        spanRecorder.recordStatusCode(statusCode);
        if (isDebug) {
            logger.debug("Record HTTP status code annotation. status-code={}", statusCode);
        }
        if (isFailed(statusCode)) {
            spanRecorder.recordError();
            if (isDebug) {
                logger.debug("Record error");
            }
        }
    }

    public boolean isFailed(final int statusCode) {
        for (StatusCode code : this.errors) {
            if (code.isCode(statusCode)) {
                return true;
            }
        }
        return false;
    }

    private interface StatusCode {
        boolean isCode(int statusCode);
    }

    private class DefaultStatusCode implements StatusCode {
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

    private class Informational implements StatusCode {
        @Override
        public boolean isCode(int statusCode) {
            return 100 <= statusCode && statusCode <= 199;
        }

        @Override
        public String toString() {
            return "1xx";
        }
    }

    private class Success implements StatusCode {
        @Override
        public boolean isCode(int statusCode) {
            return 200 <= statusCode && statusCode <= 299;
        }

        @Override
        public String toString() {
            return "2xx";
        }

    }

    private class Redirection implements StatusCode {
        @Override
        public boolean isCode(int statusCode) {
            return 300 <= statusCode && statusCode <= 399;
        }

        @Override
        public String toString() {
            return "3xx";
        }

    }

    private class ClientError implements StatusCode {
        @Override
        public boolean isCode(int statusCode) {
            return 400 <= statusCode && statusCode <= 499;
        }

        @Override
        public String toString() {
            return "4xx";
        }

    }

    private class ServerError implements StatusCode {
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
        final StringBuilder sb = new StringBuilder("HttpStatusCodeRecorder{");
        sb.append("errors=").append(Arrays.toString(errors));
        sb.append('}');
        return sb.toString();
    }
}