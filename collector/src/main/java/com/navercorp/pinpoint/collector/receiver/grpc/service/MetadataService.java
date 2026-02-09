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

import com.navercorp.pinpoint.collector.handler.RequestResponseHandler;
import com.navercorp.pinpoint.common.server.io.MessageType;
import com.navercorp.pinpoint.common.server.io.MessageTypes;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.MetadataGrpc;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PExceptionMetaData;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
import com.navercorp.pinpoint.grpc.trace.PSqlUidMetaData;
import com.navercorp.pinpoint.grpc.trace.PStringMetaData;
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

    private final RequestResponseHandler<PApiMetaData, PResult> apiMetaDataHandler;
    private final RequestResponseHandler<PSqlMetaData, PResult> sqlMetaDataHandler;
    private final RequestResponseHandler<PSqlUidMetaData, PResult> sqlUidMetaDataHandler;
    private final RequestResponseHandler<PStringMetaData, PResult> stringMetaDataHandler;
    private final RequestResponseHandler<PExceptionMetaData, PResult> exceptionMetaDataHandler;

    private final JobRunner jobRunner;
    private final Executor executor;

    public MetadataService(RequestResponseHandler<PApiMetaData, PResult> apiMetaDataHandler,
                           RequestResponseHandler<PSqlMetaData, PResult> sqlMetaDataHandler,
                           RequestResponseHandler<PSqlUidMetaData, PResult> sqlUidMetaDataHandler,
                           RequestResponseHandler<PStringMetaData, PResult> stringMetaDataHandler,
                           RequestResponseHandler<PExceptionMetaData, PResult> exceptionMetaDataHandler,
                           Executor executor,
                           ServerRequestFactory requestFactory,
                           ServerResponseFactory responseFactory) {
        this.apiMetaDataHandler = Objects.requireNonNull(apiMetaDataHandler, "apiMetaDataHandler");
        this.sqlMetaDataHandler = Objects.requireNonNull(sqlMetaDataHandler, "sqlMetaDataHandler");
        this.sqlUidMetaDataHandler = Objects.requireNonNull(sqlUidMetaDataHandler, "sqlUidMetaDataHandler");
        this.stringMetaDataHandler = Objects.requireNonNull(stringMetaDataHandler, "stringMetaDataHandler");
        this.exceptionMetaDataHandler = Objects.requireNonNull(exceptionMetaDataHandler, "exceptionMetaDataHandler");


        Objects.requireNonNull(executor, "executor");
        this.executor = Context.currentContextExecutor(executor);
        this.jobRunner = new JobRunner(logger, requestFactory, responseFactory);
    }


    @Override
    public void requestApiMetaData(PApiMetaData apiMetaData, StreamObserver<PResult> responseObserver) {
        if (isDebug) {
            logger.debug("Request PApiMetaData={}", debugLog(apiMetaData));
        }
        MessageType messageType = MessageTypes.APIMETADATA;
        doExecute(() -> {
            jobRunner.execute(messageType, apiMetaData, responseObserver,
                    apiMetaDataHandler::handleRequest);
        }, messageType);
    }

    @Override
    public void requestSqlMetaData(PSqlMetaData sqlMetaData, StreamObserver<PResult> responseObserver) {
        if (isDebug) {
            logger.debug("Request PSqlMetaData={}", debugLog(sqlMetaData));
        }
        MessageType messageType = MessageTypes.SQLMETADATA;
        doExecute(() -> {
            jobRunner.execute(messageType, sqlMetaData, responseObserver,
                    sqlMetaDataHandler::handleRequest);
        }, messageType);
    }

    @Override
    public void requestSqlUidMetaData(PSqlUidMetaData sqlUidMetaData, StreamObserver<PResult> responseObserver) {
        if (isDebug) {
            logger.debug("Request PSqlUidMetaData={}", debugLog(sqlUidMetaData));
        }
        MessageType messageType = MessageTypes.SQLUIDMETADATA;
        doExecute(() -> {
            jobRunner.execute(messageType, sqlUidMetaData, responseObserver,
                    sqlUidMetaDataHandler::handleRequest);
        }, messageType);
    }

    @Override
    public void requestStringMetaData(PStringMetaData stringMetaData, StreamObserver<PResult> responseObserver) {
        if (isDebug) {
            logger.debug("Request PStringMetaData={}", debugLog(stringMetaData));
        }
        MessageType messageType = MessageTypes.STRINGMETADATA;
        doExecute(() -> {
            jobRunner.execute(messageType, stringMetaData, responseObserver,
                    stringMetaDataHandler::handleRequest);
        }, messageType);
    }

    @Override
    public void requestExceptionMetaData(PExceptionMetaData exceptionMetaData, StreamObserver<PResult> responseObserver) {
        if (isDebug) {
            logger.debug("Request PExceptionMetaData={}", debugLog(exceptionMetaData));
        }
        MessageType messageType = MessageTypes.EXCEPTIONMETADATA;
        doExecute(() -> {
            jobRunner.execute(messageType, exceptionMetaData, responseObserver,
                    exceptionMetaDataHandler::handleRequest);
        }, messageType);
    }



    void doExecute(Runnable runnable, MessageType messageType) {
        try {
            executor.execute(runnable);
        } catch (RejectedExecutionException ree) {
            final Header header = ServerContext.getAgentInfo();
            // Defense code
            logger.warn("Failed to request. Rejected execution, {} {} executor={}", messageType, header, executor);
        }
    }
}