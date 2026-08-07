package com.navercorp.pinpoint.io.request;

/**
 * The serviceName is not registered: the uid lookup completed normally but found nothing.
 * Recoverable once the service is registered, unlike other {@link UidException}s.
 */
public class UidNotFoundException extends UidException {

    private final String serviceName;

    public UidNotFoundException(String serviceName) {
        super("Service not found. serviceName:" + serviceName);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

}