package com.navercorp.pinpoint.common.config.util;

public interface ValueResolver {
    String resolve(String key, String value);
}
