/*
 * Copyright 2024 NAVER Corp.
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
import com.navercorp.pinpoint.common.profiler.message.EnhancedDataSender;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.trace.MetadataGrpc;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PExceptionMetaData;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
import com.navercorp.pinpoint.grpc.trace.PSqlUidMetaData;
import com.navercorp.pinpoint.grpc.trace.PStringMetaData;
import com.navercorp.pinpoint.io.ResponseMessage;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

/**
 */
public class MetadataGrpcHedgingDataSender<T> extends AbstractGrpcDataSender<T> implements EnhancedDataSender<T, ResponseMessage> {

    private final MetadataGrpc.MetadataStub metadataStub;

    private final AtomicLong requestIdGen = new AtomicLong(0);

    public MetadataGrpcHedgingDataSender(String host, int port,
                                         MessageConverter<T, GeneratedMessageV3> messageConverter,
                                         ChannelFactory channelFactory) {
        super(host, port, messageConverter, channelFactory);

        this.metadataStub = MetadataGrpc.newStub(managedChannel);
    }

    // Unsupported Operation
    @Override
    public boolean request(T data, int retry) {
        throw new UnsupportedOperationException("unsupported operation request(data, retry)");
    }

    @Override
    public boolean request(T data, BiConsumer<ResponseMessage, Throwable> listener) {
        throw new UnsupportedOperationException("unsupported operation request(data, listener)");
    }

    @Override
    public boolean send(T data) {
        throw new UnsupportedOperationException("unsupported operation send(data)");
    }

    @Override
    public boolean request(final T data) {
        try {
            final GeneratedMessageV3 message = messageConverter.toMessage(data);

            if (message instanceof PSqlMetaData) {
                final PSqlMetaData sqlMetaData = (PSqlMetaData) message;
                this.metadataStub.requestSqlMetaData(sqlMetaData, newLogStreamObserver(sqlMetaData));
            } else if (message instanceof PSqlUidMetaData) {
                final PSqlUidMetaData sqlUidMetaData = (PSqlUidMetaData) message;
                this.metadataStub.requestSqlUidMetaData(sqlUidMetaData, newLogStreamObserver(sqlUidMetaData));
            } else if (message instanceof PApiMetaData) {
                final PApiMetaData apiMetaData = (PApiMetaData) message;
                this.metadataStub.requestApiMetaData(apiMetaData, newLogStreamObserver(apiMetaData));
            } else if (message instanceof PStringMetaData) {
                final PStringMetaData stringMetaData = (PStringMetaData) message;
                this.metadataStub.requestStringMetaData(stringMetaData, newLogStreamObserver(stringMetaData));
            } else if (message instanceof PExceptionMetaData) {
                final PExceptionMetaData exceptionMetaData = (PExceptionMetaData) message;
                this.metadataStub.requestExceptionMetaData(exceptionMetaData, newLogStreamObserver(exceptionMetaData));
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("Unsupported message {}", MessageFormatUtils.debugLog(message));
                }
            }
        } catch (Throwable e) {
            logger.info("Failed to send metadata={}", data, e);
            return false;
        }
        return true;
    }

    private StreamObserver<PResult> newLogStreamObserver(GeneratedMessageV3 message) {
        String type = message.getClass().getSimpleName();
        long requestId = this.requestIdGen.incrementAndGet();
        return new LogResponseStreamObserver<>(logger, type, requestId);
    }

    @Override
    public void stop() {
        if (shutdown) {
            return;
        }
        this.shutdown = true;

        super.releaseChannel();
    }
}