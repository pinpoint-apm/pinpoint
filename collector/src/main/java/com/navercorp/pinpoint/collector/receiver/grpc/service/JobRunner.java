package com.navercorp.pinpoint.collector.receiver.grpc.service;


import com.navercorp.pinpoint.io.request.ServerHeader;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.io.util.MessageType;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.BiConsumer;

public class JobRunner {
    private final Logger logger;

    private final ServerRequestFactory requestFactory;
    private final ServerResponseFactory responseFactory;

    public JobRunner(Logger logger,
                     ServerRequestFactory requestFactory,
                     ServerResponseFactory responseFactory) {
        this.logger = Objects.requireNonNull(logger, "logger");

        this.requestFactory = Objects.requireNonNull(requestFactory, "requestFactory");
        this.responseFactory = Objects.requireNonNull(responseFactory, "responseFactory");
    }

    public <Req, Res> void execute(MessageType messageType,
                                   Req req, StreamObserver<Res> responseObserver,
                                   BiConsumer<ServerRequest<Req>, ServerResponse<Res>> job) {
        Context current = Context.current();
        ServerRequest<Req> request = requestFactory.newServerRequest(current, messageType, req);
        ServerResponse<Res> response = responseFactory.newServerResponse(request, responseObserver);
        try {
            job.accept(request, response);
        } catch (Throwable e) {
            handleException(request, response, e);
        }
    }

    protected <Req, Res> void handleException(ServerRequest<Req> request, ServerResponse<Res> response, Throwable th) {
        MessageType messageType = request.getMessageType();
        ServerHeader header = request.getHeader();
        logger.warn("Failed to request. {} header={}", messageType, header, th);
        response.onError(th);
    }

}
