package com.navercorp.pinpoint.common.server.util.concurrent;

/**
 * @author Taejin Koo
 */
public enum PinpointExecutorType {

    DEFAULT_EXECUTOR,
    DISRUPTOR_EXECUTOR;

    public static PinpointExecutorType getValue(String value) {
        for (PinpointExecutorType each : PinpointExecutorType.values()) {
            if (each.name().equalsIgnoreCase(value)) {
                return each;
            }
        }

        return DEFAULT_EXECUTOR;
    }

}
