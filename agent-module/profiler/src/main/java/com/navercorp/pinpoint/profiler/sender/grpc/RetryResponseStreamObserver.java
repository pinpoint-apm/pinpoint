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

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

import static com.navercorp.pinpoint.grpc.MessageFormatUtils.debugLog;
import static com.navercorp.pinpoint.grpc.MessageFormatUtils.getSimpleClasName;

/**
 * @author Woonduk Kang(emeroad)
 */
public class RetryResponseStreamObserver<ReqT, ResT> implements StreamObserver<ResT> {
    private final Logger logger;
    private final RetryScheduler<ReqT, ResT> retryScheduler;
    private final ReqT message;
    private final int retryCount;

    public RetryResponseStreamObserver(Logger logger, RetryScheduler<ReqT, ResT> retryScheduler, ReqT message, int retryCount) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.retryScheduler = Objects.requireNonNull(retryScheduler, "retryScheduler");
        this.message = Objects.requireNonNull(message, "message");
        this.retryCount = retryCount;
    }

    @Override
    public void onNext(ResT response) {
        if (retryScheduler.isSuccess(response)) {
            // Success
            if (logger.isDebugEnabled()) {
                logger.debug("Request success. request={}, result={}", debugLog(message), debugLog((response)));
            }
        } else {
            // Retry
            if (logger.isInfoEnabled()) {
                logger.info("Request failed. PResult.getSuccess() is false. request={}, result={}", debugLog(message), debugLog((response)));
            }
            retryScheduler.scheduleNextRetry(message, nextRetryCount());
        }
    }


    @Override
    public void onError(Throwable throwable) {
        final Status status = Status.fromThrowable(throwable);
        final Metadata metadata = Status.trailersFromThrowable(throwable);

        if (logger.isDebugEnabled()) {
            logger.debug("onError. request={}, {} metadata={}", debugLog(message), status, metadata);
        } else if (logger.isInfoEnabled()) {
            logger.info("onError. request={}, {} metadata={}", getSimpleClasName(message), status, metadata);
        }

        // Retry
        retryScheduler.scheduleNextRetry(message, nextRetryCount());
    }

    @Override
    public void onCompleted() {
    }

    private int nextRetryCount() {
        return retryCount + 1;
    }



}
