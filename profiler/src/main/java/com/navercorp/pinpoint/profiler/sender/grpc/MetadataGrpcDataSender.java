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
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.trace.MetadataGrpc;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
import com.navercorp.pinpoint.grpc.trace.PStringMetaData;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.rpc.FutureListener;
import com.navercorp.pinpoint.rpc.client.PinpointClientReconnectEventListener;

import io.grpc.stub.StreamObserver;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.grpc.MessageFormatUtils.debugLog;

/**
 * @author jaehong.kim
 */
public class MetadataGrpcDataSender extends GrpcDataSender implements EnhancedDataSender<Object> {
    private final MetadataGrpc.MetadataStub metadataStub;
    private final int maxAttempts;
    private final int retryDelayMillis;

    private final Timer retryTimer;
    private static final long MAX_PENDING_TIMEOUTS = 1024 * 4;

    private final RetryScheduler<GeneratedMessageV3, PResult> retryScheduler;

    public MetadataGrpcDataSender(String host, int port, int executorQueueSize, MessageConverter<GeneratedMessageV3> messageConverter, ChannelFactory channelFactory, int retryMaxCount, int retryDelayMillis) {
        super(host, port, executorQueueSize, messageConverter, channelFactory);

        this.maxAttempts = getMaxAttempts(retryMaxCount);
        this.retryDelayMillis = retryDelayMillis;
        this.metadataStub = MetadataGrpc.newStub(managedChannel);

        this.retryTimer = newTimer("metadata-timer");

        this.retryScheduler = new RetryScheduler<GeneratedMessageV3, PResult>() {
            @Override
            public boolean isSuccess(PResult response) {
                return response.getSuccess();
            }

            @Override
            public void scheduleNextRetry(GeneratedMessageV3 request, int remainingRetryCount) {
                MetadataGrpcDataSender.this.scheduleNextRetry(request, remainingRetryCount);
            }
        };
    }

    private int getMaxAttempts(int retryMaxCount) {
        if (retryMaxCount < 0) {
            return 0;
        }
        return retryMaxCount;
    }

    private Timer newTimer(String name) {
        ThreadFactory threadFactory = new PinpointThreadFactory(PinpointThreadFactory.DEFAULT_THREAD_NAME_PREFIX + name, true);
        return new HashedWheelTimer(threadFactory, 100, TimeUnit.MILLISECONDS, 512, false, MAX_PENDING_TIMEOUTS);
    }

    // Unsupported Operation
    @Override
    public boolean request(Object data, int retry) {
        throw new UnsupportedOperationException("unsupported operation request(data, retry)");
    }

    @Override
    public boolean request(Object data, FutureListener listener) {
        throw new UnsupportedOperationException("unsupported operation request(data, listener)");
    }

    @Override
    public boolean send(Object data) {
        throw new UnsupportedOperationException("unsupported operation send(data)");
    }

    @Override
    public boolean addReconnectEventListener(PinpointClientReconnectEventListener eventListener) {
        throw new UnsupportedOperationException("unsupported operation addReconnectEventListener(eventListener)");
    }

    @Override
    public boolean removeReconnectEventListener(PinpointClientReconnectEventListener eventListener) {
        throw new UnsupportedOperationException("unsupported operation removeReconnectEventListener(eventListener)");
    }

    @Override
    public boolean request(final Object data) {
        final Runnable convertAndRun = new Runnable() {
            @Override
            public void run() {
                try {
                    // Convert message
                    final GeneratedMessageV3 message = messageConverter.toMessage(data);
                    if (isDebug) {
                        logger.debug("Request metadata={}", debugLog(message));
                    }
                    request0(message, maxAttempts);
                } catch (Exception ex) {
                    logger.info("Failed to request metadata={}", data, ex);
                }
            }
        };
        try {
            executor.execute(convertAndRun);
        } catch (RejectedExecutionException reject) {
            logger.info("Rejected metadata={}", data);
            return false;
        }
        return true;
    }

    // Request
    private void request0(final GeneratedMessageV3 message, final int remainingRetryCount) {
        if (message instanceof PSqlMetaData) {
            final PSqlMetaData sqlMetaData = (PSqlMetaData) message;
            final StreamObserver<PResult> responseObserver = newResponseStream(message, remainingRetryCount);
            this.metadataStub.requestSqlMetaData(sqlMetaData, responseObserver);
        } else if (message instanceof PApiMetaData) {
            final PApiMetaData apiMetaData = (PApiMetaData) message;
            final StreamObserver<PResult> responseObserver = newResponseStream(message, remainingRetryCount);
            this.metadataStub.requestApiMetaData(apiMetaData, responseObserver);
        } else if (message instanceof PStringMetaData) {
            final PStringMetaData stringMetaData = (PStringMetaData) message;
            final StreamObserver<PResult> responseObserver = newResponseStream(message, remainingRetryCount);
            this.metadataStub.requestStringMetaData(stringMetaData, responseObserver);
        } else {
            logger.warn("Unsupported message {}", debugLog(message));
        }
    }

    private StreamObserver<PResult> newResponseStream(GeneratedMessageV3 message, int remainingRetryCount) {
        return new RetryResponseStreamObserver<GeneratedMessageV3, PResult>(logger, retryScheduler, message, remainingRetryCount);
    }

    // Retry
    private void scheduleNextRetry(final GeneratedMessageV3 message, final int remainingRetryCount) {
        if (shutdown) {
            if (isDebug) {
                logger.debug("Request drop. Already shutdown request={}", MessageFormatUtils.debugLog(message));
            }
            return;
        }
        if (remainingRetryCount <= 0) {
            if (isDebug) {
                logger.debug("Request drop. remainingRetryCount={}, request={}", MessageFormatUtils.debugLog(message), remainingRetryCount);
            }
            return;
        }

        if (isDebug) {
            logger.debug("Request retry. request={}, remainingRetryCount={}", MessageFormatUtils.debugLog(message), remainingRetryCount);
        }
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                if (timeout.cancel()) {
                    return;
                }
                if (shutdown) {
                    return;
                }
                request0(message, remainingRetryCount);
            }
        };

        try {
            retryTimer.newTimeout(timerTask, retryDelayMillis, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            logger.debug("retry fail {}", e.getCause(), e);
        }
    }



    @Override
    public void stop() {
        if (shutdown) {
            return;
        }
        this.shutdown = true;

        final Timer retryTimer = this.retryTimer;
        if (retryTimer != null) {
            retryTimer.stop();
        }
        super.release();
    }
}