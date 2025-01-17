package com.navercorp.pinpoint.common.profiler.message;

@FunctionalInterface
public interface DataConsumer<REQ> {
    boolean send(REQ data);
}
