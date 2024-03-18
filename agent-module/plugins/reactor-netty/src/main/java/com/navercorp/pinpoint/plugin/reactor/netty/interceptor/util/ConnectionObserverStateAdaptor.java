package com.navercorp.pinpoint.plugin.reactor.netty.interceptor.util;

import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import reactor.netty.ConnectionObserver;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ConnectionObserverStateAdaptor implements ConnectionObserverAdaptor {
    @Override
    public boolean isReceived(Object[] args) {
        final ConnectionObserver.State state = getConnectionObserverState(args);
        if (state == ConnectionObserver.State.CONFIGURED) {
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
        if (state == ConnectionObserver.State.DISCONNECTING || state == ConnectionObserver.State.ACQUIRED || state == ConnectionObserver.State.RELEASED) {
            return true;
        }
        return false;
    }

    private static ConnectionObserver.State getConnectionObserverState(Object[] args) {
        return ArrayArgumentUtils.getArgument(args, 1, ConnectionObserver.State.class);
    }
}
