package com.navercorp.pinpoint.plugin.reactor.netty.interceptor.util;

import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.server.HttpServerState;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ConnectionObserverHttpServerStateAdaptor implements ConnectionObserverAdaptor {

    @Override
    public boolean isReceived(Object[] args) {
        final ConnectionObserver.State state = getConnectionObserverState(args);
        // The request was received
        if (state == HttpServerState.REQUEST_RECEIVED) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isClosed(Object[] args) {
        final ConnectionObserver.State state = getConnectionObserverState(args);
        if (state == null) {
            return false;
        }
        // ACQUIRED: Propagated when a connection has been reused / acquired (keep-alive or pooling)
        // RELEASED: Propagated when a connection has been released but not fully closed (keep-alive or pooling)
        // DISCONNECTING: Propagated when a connection is being fully closed
        if (state == HttpServerState.DISCONNECTING || state == HttpServerState.ACQUIRED || state == HttpServerState.RELEASED) {
            return true;
        }
        return false;
    }

    private static ConnectionObserver.State getConnectionObserverState(Object[] args) {
        return ArrayArgumentUtils.getArgument(args, 1, ConnectionObserver.State.class);
    }
}
