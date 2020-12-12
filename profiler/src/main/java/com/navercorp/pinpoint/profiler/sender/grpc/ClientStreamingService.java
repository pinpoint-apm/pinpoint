package com.navercorp.pinpoint.profiler.sender.grpc;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.sender.grpc.stream.ClientStreamingProvider;
import com.navercorp.pinpoint.profiler.sender.grpc.stream.StreamJob;
import io.grpc.stub.ClientCallStreamObserver;

public class ClientStreamingService<ReqT, ResT> {
    private final ClientStreamingProvider<ReqT, ResT> clientStreamingProvider;
    private final Reconnector reconnector;

    public ClientStreamingService(ClientStreamingProvider<ReqT, ResT> clientStreamingProvider,
                                  Reconnector reconnector) {
        this.clientStreamingProvider = Assert.requireNonNull(clientStreamingProvider, "clientStreamingProvider");
        this.reconnector = Assert.requireNonNull(reconnector, "reconnector");
    }

    public ClientCallStreamObserver<ReqT> newStream(StreamJob<ReqT> streamJob) {
        ResponseStreamObserver<ReqT, ResT> response = newResponse(streamJob);
        return clientStreamingProvider.newStream(response);
    }


    private ResponseStreamObserver<ReqT, ResT> newResponse(StreamJob<ReqT> streamJob) {
        StreamEventListener<ReqT> listener = new DefaultStreamEventListener<ReqT>(reconnector, streamJob);
        return new ResponseStreamObserver<ReqT, ResT>(listener);
    }
}
