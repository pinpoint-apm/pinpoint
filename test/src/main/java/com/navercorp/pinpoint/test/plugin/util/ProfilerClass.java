package com.navercorp.pinpoint.test.plugin.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProfilerClass {
    public static final List<String> PINPOINT_PROFILER_CLASS;

    static {
        String[] lib = new String[]{
                "com.navercorp.pinpoint.common",
                "com.navercorp.pinpoint.bootstrap",
                "com.navercorp.pinpoint.profiler",
                // junit
                "junit",
                "org.junit"
        };

        PINPOINT_PROFILER_CLASS = Collections.unmodifiableList(Arrays.asList(lib));
    }
}