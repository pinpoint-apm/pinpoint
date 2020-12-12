package io.grpc.netty;

import io.grpc.internal.ServerListener;

public interface ServerListenerDelegator {
    ServerListener wrapServerListener(ServerListener serverListener);
}
