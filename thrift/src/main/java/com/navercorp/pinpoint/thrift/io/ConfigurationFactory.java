package com.navercorp.pinpoint.thrift.io;

import org.apache.thrift.TConfiguration;

public class ConfigurationFactory {
    private static final TConfiguration CONFIG = new TConfiguration(
            TConfiguration.DEFAULT_MAX_MESSAGE_SIZE,
            TConfiguration.DEFAULT_MAX_FRAME_SIZE,
            TConfiguration.DEFAULT_RECURSION_DEPTH);

    public static TConfiguration getConfiguration() {
        return CONFIG;
    }
}
