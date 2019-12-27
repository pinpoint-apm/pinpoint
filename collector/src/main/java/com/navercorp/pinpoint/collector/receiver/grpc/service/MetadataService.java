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

package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.grpc.trace.MetadataGrpc;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
import com.navercorp.pinpoint.grpc.trace.PStringMetaData;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.HeaderEntity;
import com.navercorp.pinpoint.io.header.v2.HeaderV2;
import com.navercorp.pinpoint.io.request.DefaultMessage;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.thrift.io.DefaultTBaseLocator;

import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import static com.navercorp.pinpoint.grpc.MessageFormatUtils.debugLog;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MetadataService extends MetadataGrpc.MetadataImplBase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final SimpleRequestHandlerAdaptor<PResult> simpleRequestHandlerAdaptor;
    private final Executor executor;

    public MetadataService(DispatchHandler dispatchHandler, Executor executor, ServerRequestFactory serverRequestFactory) {
        Objects.requireNonNull(dispatchHandler, "dispatchHandler");
        Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(serverRequestFactory, "serverRequestFactory");

        this.executor = Context.currentContextExecutor(executor);
        this.simpleRequestHandlerAdaptor = new SimpleRequestHandlerAdaptor<>(this.getClass().getName(), dispatchHandler, serverRequestFactory);
    }


    @Override
    public void requestApiMetaData(PApiMetaData apiMetaData, StreamObserver<PResult> responseObserver) {
        if (isDebug) {
            logger.debug("Request PApiMetaData={}", debugLog(apiMetaData));
        }

        final Message<PApiMetaData> message = newMessage(apiMetaData, DefaultTBaseLocator.APIMETADATA);
        doExecutor(message, responseObserver);
    }

    @Override
    public void requestSqlMetaData(PSqlMetaData sqlMetaData, StreamObserver<PResult> responseObserver) {
        if (isDebug) {
            logger.debug("Request PSqlMetaData={}", debugLog(sqlMetaData));
        }

        final Message<PSqlMetaData> message = newMessage(sqlMetaData, DefaultTBaseLocator.SQLMETADATA);
        doExecutor(message, responseObserver);
    }

    @Override
    public void requestStringMetaData(PStringMetaData stringMetaData, StreamObserver<PResult> responseObserver) {
        if (isDebug) {
            logger.debug("Request PStringMetaData={}", debugLog(stringMetaData));
        }

        final Message<PStringMetaData> message = newMessage(stringMetaData, DefaultTBaseLocator.STRINGMETADATA);
        doExecutor(message, responseObserver);
    }

    private <T> Message<T> newMessage(T requestData, short type) {
        final Header header = new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, type);
        final HeaderEntity headerEntity = new HeaderEntity(Collections.emptyMap());
        return new DefaultMessage<>(header, headerEntity, requestData);
    }

    void doExecutor(final Message message, final StreamObserver<PResult> responseObserver) {
        try {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    simpleRequestHandlerAdaptor.request(message, responseObserver);
                }
            });
        } catch (RejectedExecutionException ree) {
            // Defense code
            logger.warn("Failed to request. Rejected execution, executor={}", executor);
        }
    }
}