package com.navercorp.pinpoint.bootstrap.config.util;

public class BypassResolver implements ValueResolver {
    public static final ValueResolver RESOLVER = new BypassResolver();

    @Override
    public String resolve(String key, String value) {
        return value;
    }
}
