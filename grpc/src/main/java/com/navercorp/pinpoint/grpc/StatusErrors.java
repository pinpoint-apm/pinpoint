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

package com.navercorp.pinpoint.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

/**
 * @author jaehong.kim
 */
public class StatusErrors {
    private static final String CONNECTION_REFUSED_MESSAGE = "Connection refused: no further information";
    private static final String CANCELLED_BEFORE_RECEIVING_HALF_CLOSE = "CANCELLED: cancelled before receiving half close";


    public static StatusError throwable(final Throwable t) {
        if (t instanceof StatusRuntimeException) {
            StatusRuntimeException exception = (StatusRuntimeException) t;
            if (exception.getStatus().getCode() == Status.UNAVAILABLE.getCode()) {
                final String causeMessage = findCauseMessage(t, CONNECTION_REFUSED_MESSAGE, 2);
                if (causeMessage != null) {
                    final String message = exception.getStatus().getDescription() + ": " + causeMessage;
                    return new SimpleStatusError(message, t);
                }
            } else if (exception.getStatus().getCode() == Status.CANCELLED.getCode()) {
                if (exception.getMessage() != null && exception.getMessage().startsWith(CANCELLED_BEFORE_RECEIVING_HALF_CLOSE)) {
                    return new SimpleStatusError(CANCELLED_BEFORE_RECEIVING_HALF_CLOSE, t);
                }
            }
        }
        return new DefaultStatusError(t);
    }

    private static String findCauseMessage(final Throwable t, final String message, final int maxDepth) {
        int depth = 0;
        Throwable cause = t.getCause();
        while (cause != null && depth < maxDepth) {
            if (cause.getMessage().startsWith(message)) {
                return cause.getMessage();
            }

            if (cause.getCause() == cause) {
                break;
            }
            cause = cause.getCause();
            depth += 1;
        }
        // Not found
        return null;
    }

    private static class SimpleStatusError implements StatusError {
        private final String message;
        private final Throwable throwable;

        public SimpleStatusError(final String message, final Throwable throwable) {
            this.message = message;
            this.throwable = throwable;
        }

        @Override
        public boolean isSimpleError() {
            return true;
        }

        @Override
        public String getMessage() {
            return this.message;
        }

        @Override
        public Throwable getThrowable() {
            return this.throwable;
        }
    }

    private static class DefaultStatusError implements StatusError {
        private final Throwable throwable;

        public DefaultStatusError(final Throwable throwable) {
            this.throwable = throwable;
        }

        @Override
        public boolean isSimpleError() {
            return false;
        }

        @Override
        public String getMessage() {
            if (this.throwable != null) {
                return this.throwable.getMessage();
            }
            return "";
        }

        @Override
        public Throwable getThrowable() {
            return this.throwable;
        }
    }
}