package com.navercorp.pinpoint.grpc.server;

/**
 * Mutable shared context per transport connection.
 * All streams on the same transport share the same instance.
 */
public class TransportMutableContext {

    private volatile int serviceType = -1;

    public int getServiceType() {
        return serviceType;
    }

    public void setServiceType(int serviceType) {
        this.serviceType = serviceType;
    }

    @Override
    public String toString() {
        return "TransportMutableContext{" +
                "serviceType=" + serviceType +
                '}';
    }
}