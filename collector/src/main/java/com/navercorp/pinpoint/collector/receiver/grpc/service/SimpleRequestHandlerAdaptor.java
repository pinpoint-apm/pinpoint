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
import com.navercorp.pinpoint.collector.receiver.grpc.GrpcServerResponse;
import com.navercorp.pinpoint.collector.receiver.grpc.retry.GrpcRetryFriendlyServerResponse;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SimpleRequestHandlerAdaptor<REQ, RES> {
    private final Logger logger;

    private final DispatchHandler<REQ, RES> dispatchHandler;

    public SimpleRequestHandlerAdaptor(String name, DispatchHandler<REQ, RES> dispatchHandler) {
        Objects.requireNonNull(name, "name");
        this.logger = LogManager.getLogger(name);
        this.dispatchHandler = Objects.requireNonNull(dispatchHandler, "dispatchHandler");
    }

    public void request(ServerRequest<? extends REQ> request, StreamObserver<? extends RES> responseObserver) {
        try {
            final ServerResponse<? extends RES> response = newServerResponse(request, responseObserver);
            this.dispatchHandler.dispatchRequestMessage((ServerRequest<REQ>) request, (ServerResponse<RES>) response);
        } catch (Exception e) {
            final Header header = request.getHeader();
            logger.warn("Failed to request. header={}", header, e);
            if (e instanceof StatusException || e instanceof StatusRuntimeException) {
                responseObserver.onError(e);
            } else {
                // Avoid detailed exception
                responseObserver.onError(Status.INTERNAL.withDescription("Bad Request").asException());
            }
        }
    }

    private ServerResponse<? extends RES> newServerResponse(ServerRequest<? extends REQ> request, StreamObserver<? extends RES> responseObserver) {
        Header header = request.getHeader();
        if (header.isGrpcBuiltInRetry()) {
            return new GrpcRetryFriendlyServerResponse<>(responseObserver);
        }
        return new GrpcServerResponse<>(responseObserver);
    }
}
