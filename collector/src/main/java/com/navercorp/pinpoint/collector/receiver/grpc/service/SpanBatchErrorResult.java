/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.navercorp.pinpoint.common.util.ErrorId;
import com.navercorp.pinpoint.grpc.trace.PPartialSuccess;
import com.navercorp.pinpoint.grpc.trace.PSpanResultBatch;

public class SpanBatchErrorResult {

    private static final int DEFAULT_ERROR_MESSAGE_LIMIT = 3;

    private final int errorMessageLimit;
    private ErrorId errorId;
    private long rejectedSpans;
    private StringBuilder errorMessages;

    public SpanBatchErrorResult() {
        this(DEFAULT_ERROR_MESSAGE_LIMIT);
    }

    public SpanBatchErrorResult(int errorMessageLimit) {
        this.errorMessageLimit = errorMessageLimit;
    }

    public ErrorId getErrorId() {
        if (errorId == null) {
            return ErrorId.EMPTY;
        }
        return errorId;
    }

    public void recordException(Throwable e) {
        rejectedSpans++;
        if (errorId == null) {
            errorId = ErrorId.random();
        }

        if (rejectedSpans <= errorMessageLimit) {
            if (errorMessages == null) {
                errorMessages = new StringBuilder();
            } else {
                errorMessages.append(", ");
            }
            errorMessages.append(e.getMessage());
        }
    }

    public PSpanResultBatch buildResultBatch() {
        if (rejectedSpans == 0) {
            return PSpanResultBatch.getDefaultInstance();
        }
        final PPartialSuccess.Builder partialSuccess = PPartialSuccess.newBuilder();
        partialSuccess.setRejectedSpans(rejectedSpans);
        partialSuccess.setErrorId(errorId.getId());

        if (errorMessages != null) {
            partialSuccess.setErrorMessage(errorMessages.toString());
        }
        return PSpanResultBatch.newBuilder()
                .setPartialSuccess(partialSuccess)
                .build();
    }
}