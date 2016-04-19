package com.navercorp.pinpoint.rpc;

/**
 * @Author Taejin Koo
 */
public abstract class PinpointDatagramSocketFactory {

    static final int SOCKET_TIMEOUT = 1000 * 5;
    static final int SEND_BUFFER_SIZE = 1024 * 64 * 16;

    public PinpointDatagramSocket createSocket() {
        return createSocket(SOCKET_TIMEOUT, SEND_BUFFER_SIZE);
    }

    public PinpointDatagramSocket createSocket(int timeout) {
        return createSocket(timeout, SEND_BUFFER_SIZE);
    }

    public abstract PinpointDatagramSocket createSocket(int timeout, int sendBufferSize);

}
