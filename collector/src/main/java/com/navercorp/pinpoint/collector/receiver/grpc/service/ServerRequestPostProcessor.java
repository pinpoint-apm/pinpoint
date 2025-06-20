package com.navercorp.pinpoint.collector.receiver.grpc.service;


import com.navercorp.pinpoint.io.request.ServerRequest;
import io.grpc.Context;

public interface ServerRequestPostProcessor {

    <T> void postProcess(Context context, ServerRequest<T> serverRequest);
}
