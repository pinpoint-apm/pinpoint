package com.navercorp.pinpoint.common.server.util.concurrent;

import com.lmax.disruptor.EventFactory;

/**
 * @author Taejin Koo
 */
public class RunnableEvent {

    static EventFactory EVENT_FACTORY = new Factory();
    private Runnable value;

    public void set(Runnable value) {
        this.value = value;
    }

    public Runnable getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "RunnableEvent{" + "value=" + value + '}';
    }

    static class Factory implements EventFactory<RunnableEvent> {

        @Override
        public RunnableEvent newInstance() {
            return new RunnableEvent();
        }

    }

}
