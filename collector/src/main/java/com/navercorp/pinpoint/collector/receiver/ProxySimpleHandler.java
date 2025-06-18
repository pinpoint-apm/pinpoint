package com.navercorp.pinpoint.collector.receiver;

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.manage.HandlerManager;
import com.navercorp.pinpoint.io.request.ServerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class ProxySimpleHandler<T> implements SimpleHandler<T> {
    private final Logger logger = LogManager.getLogger(this.getClass());


    private final SimpleHandler<T> delegate;
    private final HandlerManager handlerManager;

    public ProxySimpleHandler(SimpleHandler<T> simpleHandler, HandlerManager handlerManager) {
        this.delegate = Objects.requireNonNull(simpleHandler, "simpleHandler");
        this.handlerManager = Objects.requireNonNull(handlerManager, "handlerManager");
    }

    @Override
    public void handleSimple(ServerRequest<T> serverRequest) {
        if (!checkAvailable()) {
            logger.debug("Handler is disabled. Skipping send message {}.", serverRequest);
            return;
        }

        this.delegate.handleSimple(serverRequest);
    }

    private boolean checkAvailable() {
        if (handlerManager.isEnable()) {
            return true;
        }

        return false;
    }
}
