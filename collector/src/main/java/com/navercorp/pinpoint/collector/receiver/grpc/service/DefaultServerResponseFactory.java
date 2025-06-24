package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.navercorp.pinpoint.collector.receiver.grpc.GrpcServerResponse;
import com.navercorp.pinpoint.collector.receiver.grpc.retry.GrpcRetryFriendlyServerResponse;
import com.navercorp.pinpoint.io.request.ServerHeader;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import io.grpc.stub.StreamObserver;

public class DefaultServerResponseFactory implements ServerResponseFactory {
    @Override
    public <REQ, RES> ServerResponse<RES> newServerResponse(ServerRequest<REQ> serverRequest, StreamObserver<RES> responseObserver) {
        ServerHeader header = serverRequest.getHeader();
        if (header.isGrpcBuiltInRetry()) {
            return new GrpcRetryFriendlyServerResponse<>(responseObserver);
        }
        return new GrpcServerResponse<>(responseObserver);
    }


}
