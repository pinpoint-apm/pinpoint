package com.navercorp.pinpoint.rpc;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Taejin Koo
 */
public class PinpointDatagramSocketFactoryLocator {

    private static final Map<PinpointDatagramSocketType, PinpointDatagramSocketFactory> FACTORY_REPOSITORY = new HashMap();

    static {
        FACTORY_REPOSITORY.put(PinpointDatagramSocketType.NIO, new PinpointNioDatagramSocketFactory());
        FACTORY_REPOSITORY.put(PinpointDatagramSocketType.OIO, new PinpointOioDatagramSocketFactory());
    }

    public PinpointDatagramSocketFactoryLocator() {
    }

    public static PinpointDatagramSocketFactory getFactory(String socketType) {
        return getFactory(PinpointDatagramSocketType.getValue(socketType));
    }

    public static PinpointDatagramSocketFactory getFactory(PinpointDatagramSocketType socketType) {
        return FACTORY_REPOSITORY.get(socketType);
    }

}
