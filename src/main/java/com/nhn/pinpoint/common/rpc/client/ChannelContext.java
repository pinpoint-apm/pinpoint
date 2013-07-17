package com.nhn.pinpoint.common.rpc.client;

/**
 *
 */
public class ChannelContext {
    private RequestManager requestManager;
    private StreamChannelManager streamChannelManager;
    private SocketHandler socketHandler;

    public SocketHandler getSocketHandler() {
        return socketHandler;
    }

    public void setSocketHandler(SocketHandler socketHandler) {
        this.socketHandler = socketHandler;
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
