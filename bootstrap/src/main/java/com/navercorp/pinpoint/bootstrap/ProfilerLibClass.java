package com.nhn.pinpoint.bootstrap;

/**
 * @author emeroad
 */
public class ProfilerLibClass {

    private static final String[] PINPOINT_PROFILER_CLASS = new String[] {
            "com.nhn.pinpoint.profiler",
            "com.nhn.pinpoint.thrift",
            "com.nhn.pinpoint.rpc",
            "javassist",
            "org.slf4j",
            "org.apache.thrift",
            "org.jboss.netty",
            "com.google.common",
            "org.apache.commons.lang",
            "org.apache.log4j",
            "com.codahale.metrics"
    };

    public boolean onLoadClass(String clazzName) {
        final int length = PINPOINT_PROFILER_CLASS.length;
        for (int i = 0; i < length; i++) {
            if (clazzName.startsWith(PINPOINT_PROFILER_CLASS[i])) {
                return true;
            }
        }
        return false;
    }
}
