package io.grpc.netty;

import io.grpc.internal.ServerListener;

public class EmptyServerListenerDelegator implements ServerListenerDelegator {
    @Override
    public ServerListener wrapServerListener(ServerListener serverListener) {
        return serverListener;
    }
}
