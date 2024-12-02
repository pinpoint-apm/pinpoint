package com.navercorp.pinpoint.common.util;

import java.util.regex.Pattern;

public final class AgentVersionPostfix {

    public static String STABLE_VERSION_PATTERN_STRING = "[0-9]+\\.[0-9]+\\.[0-9](-p[0-9]+)?";
    public static final Pattern STABLE_VERSION_PATTERN = Pattern.compile(STABLE_VERSION_PATTERN_STRING);

    private AgentVersionPostfix() {
    }

    public static boolean isStableVersion(String version) {
        return STABLE_VERSION_PATTERN.matcher(version).matches();
    }

}
