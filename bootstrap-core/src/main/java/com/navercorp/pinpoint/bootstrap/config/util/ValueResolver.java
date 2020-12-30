package com.navercorp.pinpoint.bootstrap.config.util;

public interface ValueResolver {
    String resolve(String key, String value);
}
