package com.navercorp.pinpoint.grpc.stream;

public class ClientCallContext {
    public enum CallType {
        REQUEST, RESPONSE
    }

    private final StreamState request = new StreamState(CallType.REQUEST);
    private final StreamState response = new StreamState(CallType.RESPONSE);

    public StreamState request() {
        return request;
    }

    public StreamState response() {
        return response;
    }

    @Override
    public String toString() {
        return "ClientCallContext{" + request + ", " + response + '}';
    }
}
