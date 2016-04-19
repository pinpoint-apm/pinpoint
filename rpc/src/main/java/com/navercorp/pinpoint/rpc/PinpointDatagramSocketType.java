package com.navercorp.pinpoint.rpc;

/**
 * @Author Taejin Koo
 */
public enum PinpointDatagramSocketType {

    NIO,
    OIO;


    public static PinpointDatagramSocketType getValue(String name) {
        PinpointDatagramSocketType[] values = values();
        for (PinpointDatagramSocketType value : values) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }

        return null;
    }
}
