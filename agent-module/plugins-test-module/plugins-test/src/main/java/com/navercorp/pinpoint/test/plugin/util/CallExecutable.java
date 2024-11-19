package com.navercorp.pinpoint.test.plugin.util;

@FunctionalInterface
public interface CallExecutable<V> {
    V call();
}
