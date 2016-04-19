package com.navercorp.pinpoint.rpc;

/**
 * @Author Taejin Koo
 */
public class PinpointOioDatagramSocketFactory extends PinpointDatagramSocketFactory {

    @Override
    public PinpointDatagramSocket createSocket(int timeout, int sendBufferSize) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("timeout");
        }
        if (sendBufferSize <= 0) {
            throw new IllegalArgumentException("sendBufferSize");
        }

        return new PinpointOioDatagramSocket(timeout, sendBufferSize);
    }

}
