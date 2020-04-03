/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.sender.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.TextFormat;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.StatusError;
import com.navercorp.pinpoint.grpc.StatusErrors;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;

/**
 * @author Woonduk Kang(emeroad)
 */
public class RetryResponseStreamObserver<ReqT, ResT> implements StreamObserver<ResT> {
    private final Logger logger;
    private final RetryScheduler<ReqT, ResT> retryScheduler;
    private final ReqT message;
    private final int remainingRetryCount;

    public RetryResponseStreamObserver(Logger logger, RetryScheduler<ReqT, ResT> retryScheduler, ReqT message, int remainingRetryCount) {
        this.logger = Assert.requireNonNull(logger, "logger");
        this.retryScheduler = Assert.requireNonNull(retryScheduler, "retryScheduler");
        this.message = Assert.requireNonNull(message, "message");
        this.remainingRetryCount = remainingRetryCount;
    }

    @Override
    public void onNext(ResT response) {
        if (retryScheduler.isSuccess(response)) {
            // Success
            if (logger.isDebugEnabled()) {
                logger.debug("Request success. request={}, result={}", logString(message), logString(response));
            }
        } else {
            // Retry
            if (logger.isInfoEnabled()) {
                logger.info("Request fail. request={}, result={}", logString(message), logString(response));
            }
            retryScheduler.scheduleNextRetry(message, nextRetryCount());
        }
    }


    @Override
    public void onError(Throwable throwable) {
        final StatusError statusError = StatusErrors.throwable(throwable);
        if (statusError.isSimpleError()) {
            logger.info("Error. request={}, cause={}", logString(message), statusError.getMessage());
        } else {
            logger.info("Error. request={}, cause={}", logString(message), statusError.getMessage(), statusError.getThrowable());
        }

        // Retry
        final int remainingRetryCount = nextRetryCount();
        retryScheduler.scheduleNextRetry(message, remainingRetryCount);
    }

    @Override
    public void onCompleted() {
    }

    private int nextRetryCount() {
        return remainingRetryCount - 1;
    }

    private String logString(Object message) {
        if (message == null) {
            return "NULL";
        }
        if (message instanceof GeneratedMessageV3) {
            GeneratedMessageV3 messageV3 = (GeneratedMessageV3) message;
            return TextFormat.shortDebugString(messageV3);
        }
        return message.toString();
    }
}
