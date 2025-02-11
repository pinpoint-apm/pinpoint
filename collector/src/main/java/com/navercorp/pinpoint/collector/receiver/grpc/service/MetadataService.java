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

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.grpc.trace.MetadataGrpc;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PExceptionMetaData;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
import com.navercorp.pinpoint.grpc.trace.PSqlUidMetaData;
import com.navercorp.pinpoint.grpc.trace.PStringMetaData;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.util.MessageType;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import static com.navercorp.pinpoint.grpc.MessageFormatUtils.debugLog;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MetadataService extends MetadataGrpc.MetadataImplBase {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ServerRequestFactory serverRequestFactory;
    private final SimpleRequestHandlerAdaptor<GeneratedMessageV3, GeneratedMessageV3> simpleRequestHandlerAdaptor;
    private final Executor executor;

    public MetadataService(DispatchHandler<GeneratedMessageV3, GeneratedMessageV3> dispatchHandler, Executor executor, ServerRequestFactory serverRequestFactory) {
        Objects.requireNonNull(dispatchHandler, "dispatchHandler");
        Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(serverRequestFactory, "serverRequestFactory");

        this.executor = Context.currentContextExecutor(executor);
        this.simpleRequestHandlerAdaptor = new SimpleRequestHandlerAdaptor<>(this.getClass().getName(), dispatchHandler);
        this.serverRequestFactory = Objects.requireNonNull(serverRequestFactory, "serverRequestFactory");
    }


    @Override
    public void requestApiMetaData(PApiMetaData apiMetaData, StreamObserver<PResult> responseObserver) {
        if (isDebug) {
            logger.debug("Request PApiMetaData={}", debugLog(apiMetaData));
        }

        ServerRequest<PApiMetaData> request = serverRequestFactory.newServerRequest(MessageType.APIMETADATA, apiMetaData);
        doExecutor(request, responseObserver);
    }

    @Override
    public void requestSqlMetaData(PSqlMetaData sqlMetaData, StreamObserver<PResult> responseObserver) {
        if (isDebug) {
            logger.debug("Request PSqlMetaData={}", debugLog(sqlMetaData));
        }
        ServerRequest<PSqlMetaData> request = serverRequestFactory.newServerRequest(MessageType.SQLMETADATA, sqlMetaData);
        doExecutor(request, responseObserver);
    }

    @Override
    public void requestSqlUidMetaData(PSqlUidMetaData sqlUidMetaData, StreamObserver<PResult> responseObserver) {
        if (isDebug) {
            logger.debug("Request PSqlUidMetaData={}", debugLog(sqlUidMetaData));
        }

        ServerRequest<PSqlUidMetaData> request = serverRequestFactory.newServerRequest(MessageType.SQLUIDMETADATA, sqlUidMetaData);
        doExecutor(request, responseObserver);
    }

    @Override
    public void requestStringMetaData(PStringMetaData stringMetaData, StreamObserver<PResult> responseObserver) {
        if (isDebug) {
            logger.debug("Request PStringMetaData={}", debugLog(stringMetaData));
        }

        ServerRequest<PStringMetaData> request = serverRequestFactory.newServerRequest(MessageType.STRINGMETADATA, stringMetaData);
        doExecutor(request, responseObserver);
    }

    @Override
    public void requestExceptionMetaData(PExceptionMetaData exceptionMetaData, StreamObserver<PResult> responseObserver) {
        if (isDebug) {
            logger.debug("Request PStringMetaData={}", debugLog(exceptionMetaData));
        }

        ServerRequest<PExceptionMetaData> request = serverRequestFactory.newServerRequest(MessageType.EXCEPTIONMETADATA, exceptionMetaData);
        doExecutor(request, responseObserver);
    }


    void doExecutor(final ServerRequest<? extends GeneratedMessageV3> request, final StreamObserver<? extends GeneratedMessageV3> responseObserver) {
        try {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    simpleRequestHandlerAdaptor.request(request, responseObserver);
                }
            });
        } catch (RejectedExecutionException ree) {
            // Defense code
            logger.warn("Failed to request. Rejected execution, executor={}", executor);
        }
    }
}