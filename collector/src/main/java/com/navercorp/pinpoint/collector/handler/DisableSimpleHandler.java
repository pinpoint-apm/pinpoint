package com.navercorp.pinpoint.collector.handler;

import com.navercorp.pinpoint.io.request.ServerRequest;

public class DisableSimpleHandler<T> implements SimpleHandler<T> {

    @Override
    public void handleSimple(ServerRequest<T> request) {
    }

    @Override
    public String toString() {
        return "DisableSimpleHandler";
    }
}
