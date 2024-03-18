package com.navercorp.pinpoint.plugin.reactor.netty.interceptor.util;

import com.navercorp.pinpoint.plugin.reactor.netty.ReactorNettyConstants;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface ConnectionObserverAdaptor {


    boolean isReceived(Object[] args);

    boolean isClosed(Object[] args);

    class Factory {
        public static ConnectionObserverAdaptor newAdaptor(int version) {
            if (version == ReactorNettyConstants.V0_0_0) {
                return new ConnectionObserverStateAdaptor();
            }
            return new ConnectionObserverHttpServerStateAdaptor();
        }
    }
}
