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

package com.navercorp.pinpoint.profiler.sender.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.client.ChannelFactoryOption;
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

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.grpc.MessageFormatUtils.debugLog;

/**
 * @author jaehong.kim
 */
public class MetadataGrpcDataSender extends GrpcDataSender implements EnhancedDataSender<Object> {
    private final MetadataGrpc.MetadataStub metadataStub;
    private final int maxAttempts;
    private final int retryDelayMillis;

    public MetadataGrpcDataSender(String host, int port, int executorQueueSize, MessageConverter<GeneratedMessageV3> messageConverter, ChannelFactoryOption channelFactoryOption, int retryMaxCount, int retryDelayMillis) {
        super(host, port, executorQueueSize, messageConverter, channelFactoryOption);

        if (retryMaxCount < 0) {
            this.maxAttempts = 1;
        } else {
            this.maxAttempts = retryMaxCount + 1;
        }
        this.retryDelayMillis = retryDelayMillis;
        this.metadataStub = MetadataGrpc.newStub(managedChannel);
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
    public boolean send0(Object data) {
        throw new UnsupportedOperationException("unsupported operation send0(data)");
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
            PSqlMetaData sqlMetaData = (PSqlMetaData) message;
            this.metadataStub.requestSqlMetaData(sqlMetaData, new RetryResponseStreamObserver(message, remainingRetryCount));
        } else if (message instanceof PApiMetaData) {
            final PApiMetaData apiMetaData = (PApiMetaData) message;
            this.metadataStub.requestApiMetaData(apiMetaData, new RetryResponseStreamObserver(message, remainingRetryCount));
        } else if (message instanceof PStringMetaData) {
            final PStringMetaData stringMetaData = (PStringMetaData) message;
            this.metadataStub.requestStringMetaData(stringMetaData, new RetryResponseStreamObserver(message, remainingRetryCount));
        } else {
            logger.warn("Unsupported message {}", debugLog(message));
        }
    }

    // Retry
    private void scheduleNextRetry(final GeneratedMessageV3 message, final int remainingRetryCount) {
        if (shutdown || remainingRetryCount <= 0) {
            if (isDebug) {
                logger.debug("Request drop. request={}, remainingRetryCount={}, shutdown={}", MessageFormatUtils.debugLog(message), remainingRetryCount, shutdown);
            }
            return;
        }

        if (isDebug) {
            logger.debug("Request retry. request={}, remainingRetryCount={}", MessageFormatUtils.debugLog(message), remainingRetryCount);
        }
        channelFactory.getEventLoopGroup().schedule(new Runnable() {
            @Override
            public void run() {
                if (!shutdown) {
                    request0(message, remainingRetryCount);
                }
            }
        }, retryDelayMillis, TimeUnit.MILLISECONDS);
    }

    private class RetryResponseStreamObserver implements StreamObserver<PResult> {
        private final GeneratedMessageV3 message;
        private final int remainingRetryCount;

        public RetryResponseStreamObserver(GeneratedMessageV3 message, int remainingRetryCount) {
            this.message = message;
            this.remainingRetryCount = remainingRetryCount - 1;
        }

        @Override
        public void onNext(PResult result) {
            if (result.getSuccess()) {
                // Success
                if (isDebug) {
                    logger.debug("Request success. request={}, result={}", MessageFormatUtils.debugLog(message), result.getMessage());
                }
            } else {
                // Retry
                logger.info("Request fail. request={}, result={}", MessageFormatUtils.debugLog(message), result.getMessage());
                scheduleNextRetry(message, remainingRetryCount);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            // Retry
            logger.info("Request error. request={}, caused={}", MessageFormatUtils.debugLog(message), throwable, throwable);
            scheduleNextRetry(message, remainingRetryCount);
        }

        @Override
        public void onCompleted() {
        }
    }
}