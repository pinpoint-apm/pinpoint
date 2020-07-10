package com.navercorp.pinpoint.common.server.profile;


import com.navercorp.pinpoint.common.util.SystemProperty;

public class Log4j2ProfileProperty {
    public static final String LOG4J2_CONFIGURATION = "log4jConfiguration";
    public static final String LOG4J2_CONFIGURATION_PROFILE = "log4jConfiguration-profiles";
    public static final String PROFILE_PLACE_HOLDER = "${" + ProfileApplicationInitializer.PINPOINT_ACTIVE_PROFILE + "}";

    private final SystemProperty systemProperty;

    public Log4j2ProfileProperty() {
        this(new SystemProperty());
    }

    public Log4j2ProfileProperty(SystemProperty systemProperty) {
        this.systemProperty = systemProperty;
    }

    public String getLog4jProfileConfiguration(String log4jConfiguration) {
        final String activeProfile = systemProperty.getProperty(ProfileApplicationInitializer.ACTIVE_PROFILES_PROPERTY_NAME,
                ProfileApplicationInitializer.PINPOINT_DEFAULT_PROFILE);

        return log4jConfiguration.replace(PROFILE_PLACE_HOLDER, activeProfile);
    }

}
