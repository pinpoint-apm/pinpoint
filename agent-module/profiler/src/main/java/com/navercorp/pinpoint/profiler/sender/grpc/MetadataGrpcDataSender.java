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
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;
import io.grpc.stub.StreamObserver;

/**
 * @author jaehong.kim
 */
public class MetadataGrpcDataSender extends GrpcDataSender<MetaDataType> {
    //
    private final MetadataGrpc.MetadataStub metadataStub;

    public MetadataGrpcDataSender(String host, int port, int executorQueueSize,
                                  MessageConverter<MetaDataType, GeneratedMessageV3> messageConverter,
                                  ChannelFactory channelFactory) {
        super(host, port, executorQueueSize, messageConverter, channelFactory);
        this.metadataStub = MetadataGrpc.newStub(managedChannel);
    }


//    private Timer newTimer(String name) {
//        ThreadFactory threadFactory = new PinpointThreadFactory(PinpointThreadFactory.DEFAULT_THREAD_NAME_PREFIX + name, true);
//        return new HashedWheelTimer(threadFactory, 100, TimeUnit.MILLISECONDS, 512, false, MAX_PENDING_TIMEOUTS);
//    }

    //send with retry
    @Override
    public boolean send(MetaDataType data) {
        try {
            final GeneratedMessageV3 message = messageConverter.toMessage(data);

            if (message instanceof PSqlMetaData) {
                final PSqlMetaData sqlMetaData = (PSqlMetaData) message;
                this.metadataStub.requestSqlMetaData(sqlMetaData, newLogStreamObserver());
            } else if (message instanceof PSqlUidMetaData) {
                final PSqlUidMetaData sqlUidMetaData = (PSqlUidMetaData) message;
                this.metadataStub.requestSqlUidMetaData(sqlUidMetaData, newLogStreamObserver());
            } else if (message instanceof PApiMetaData) {
                final PApiMetaData apiMetaData = (PApiMetaData) message;
                this.metadataStub.requestApiMetaData(apiMetaData, newLogStreamObserver());
            } else if (message instanceof PStringMetaData) {
                final PStringMetaData stringMetaData = (PStringMetaData) message;
                this.metadataStub.requestStringMetaData(stringMetaData, newLogStreamObserver());
            } else if (message instanceof PExceptionMetaData) {
                final PExceptionMetaData exceptionMetaData = (PExceptionMetaData) message;
                this.metadataStub.requestExceptionMetaData(exceptionMetaData, newLogStreamObserver());
            } else {
                logger.warn("Unsupported message {}", MessageFormatUtils.debugLog(message));
            }
        } catch (Exception e) {
            logger.info("Failed to send metadata={}", data, e);
            return false;
        }
        return true;
    }

    private StreamObserver<PResult> newLogStreamObserver() {
        return new LogResponseStreamObserver<>(logger);
    }

    public boolean request(final MetaDataType data) {
        return this.send(data);
    }

    @Override
    public void stop() {
        if (shutdown) {
            return;
        }
        this.shutdown = true;

        super.release();
    }
}