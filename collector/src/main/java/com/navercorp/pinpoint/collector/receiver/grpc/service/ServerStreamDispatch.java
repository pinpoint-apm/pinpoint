package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.google.protobuf.GeneratedMessageV3;

public interface ServerStreamDispatch<Req extends GeneratedMessageV3, Res extends GeneratedMessageV3> {
    void onNext(Req req, ServerCallStream<Req, Res> stream);
}
