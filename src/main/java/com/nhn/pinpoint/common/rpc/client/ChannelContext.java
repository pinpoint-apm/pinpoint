package com.nhn.pinpoint.common.rpc.client;

/**
 *
 */
public class ChannelContext {
    private RequestManager requestManager;
    private StreamChannelManager streamChannelManager;
    private PinpointSocket pinpointSocket;

    public PinpointSocket getPinpointSocket() {
        return pinpointSocket;
    }

    public void setPinpointSocket(PinpointSocket pinpointSocket) {
        this.pinpointSocket = pinpointSocket;
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }

    public void setRequestManager(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    public StreamChannelManager getStreamChannelManager() {
        return streamChannelManager;
    }

    public void setStreamChannelManager(StreamChannelManager streamChannelManager) {
        this.streamChannelManager = streamChannelManager;
    }
}
