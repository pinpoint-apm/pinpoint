package com.navercorp.pinpoint.collector.receiver;

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.manage.HandlerManager;

import java.util.Objects;

public class SimpleHandlerProxy {
    private final HandlerManager handlerManager;

    public SimpleHandlerProxy(HandlerManager handlerManager) {
        this.handlerManager = Objects.requireNonNull(handlerManager, "handlerManager");
    }

    public <T> SimpleHandler<T> proxy(SimpleHandler<T> handler) {
        return new ProxySimpleHandler<>(handler, handlerManager);
    }

    @Override
    public String toString() {
        return "SimpleHandlerProxy";
    }
}
