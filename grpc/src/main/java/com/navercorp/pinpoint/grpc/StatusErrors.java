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

import com.navercorp.pinpoint.common.util.StringUtils;
import io.grpc.Status;

/**
 * @author jaehong.kim
 */
@Deprecated
public class StatusErrors {
    static final String CONNECTION_REFUSED_MESSAGE = "Connection refused: no further information";
    static final String CANCELLED_BEFORE_RECEIVING_HALF_CLOSE = "CANCELLED: cancelled before receiving half close";

    //    @NonNull
    public static StatusError throwable(final Throwable throwable) {
        final Status status = Status.fromThrowable(throwable);
        if (status != Status.UNKNOWN) {
            try {
                final SimpleStatusError message = getSimpleStatusError(status, throwable);
                if (message != null) {
                    return message;
                }
            } catch (Throwable parseError) {
                return new DefaultStatusError(new RuntimeException("Status parse error", parseError));
            }
        }
        return new DefaultStatusError(throwable);
    }

    private static SimpleStatusError getSimpleStatusError(Status status, Throwable throwable) {
        final int code = status.getCode().value();
        if (code == Status.UNAVAILABLE.getCode().value()) {
            final String causeMessage = findCauseMessage(throwable, CONNECTION_REFUSED_MESSAGE, 2);
            if (causeMessage != null) {
                final String message = status.getDescription() + ": " + causeMessage;
                return new SimpleStatusError(message, throwable);
            }
        } else if (code == Status.CANCELLED.getCode().value()) {
            final String message = throwable.getMessage();
            if (StringUtils.contains(message, CANCELLED_BEFORE_RECEIVING_HALF_CLOSE)) {
                return new SimpleStatusError(CANCELLED_BEFORE_RECEIVING_HALF_CLOSE, throwable);
            }
        }
        return null;
    }

    private static String findCauseMessage(final Throwable t, final String message, final int maxDepth) {
        int depth = 0;
        Throwable cause = t.getCause();
        while (cause != null && depth < maxDepth) {
            String causeMessage = cause.getMessage();
            if (StringUtils.startWith(causeMessage, message)) {
                return causeMessage;
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