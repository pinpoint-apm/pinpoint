package com.navercorp.pinpoint.bootstrap.util;

public class ProfileConstants {

    public static final String CONFIG_LOAD_MODE_KEY = "pinpoint.config.load.mode";

    public enum CONFIG_LOAD_MODE {
        PROFILE,
        // for IT TEST
        SIMPLE
    }

    public static final String ACTIVE_PROFILE_KEY = "pinpoint.profiler.profiles.active";

    public static final String PROFILE_ALIAS_KEY_PREFIX = "pinpoint.profiler.profiles.aliases.";

    // 1. default config
    public static final String CONFIG_FILE_NAME = "pinpoint-root.config";
    // 2. profile config
    public static final String PROFILE_CONFIG_FILE_NAME = "pinpoint.config";
    // 3. external config
    public static final String EXTERNAL_CONFIG_KEY = "pinpoint.config";
}
