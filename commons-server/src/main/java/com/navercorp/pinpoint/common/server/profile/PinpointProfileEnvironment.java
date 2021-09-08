package com.navercorp.pinpoint.common.server.profile;

import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class PinpointProfileEnvironment {
    public static final String PINPOINT_ACTIVE_PROFILE = "pinpoint.profiles.active";
    public static final String PROFILE_PLACE_HOLDER = "${" + PINPOINT_ACTIVE_PROFILE + "}";

    private final ServerBootLogger logger = ServerBootLogger.getLogger(getClass());

    private final String defaultProfile;

    public PinpointProfileEnvironment() {
        this.defaultProfile = null;
    }

    public PinpointProfileEnvironment(String defaultProfile) {
        this.defaultProfile = Objects.requireNonNull(defaultProfile, "defaultProfile");
    }

    public void processEnvironment(ConfigurableEnvironment environment) {
        String[] activeProfiles = environment.getActiveProfiles();

        logger.info(org.springframework.core.env.AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME + ":" + Arrays.toString(activeProfiles));

        final String pinpointProfile = getDefaultProfile(activeProfiles);
        logger.info(String.format("%s=%s", PINPOINT_ACTIVE_PROFILE, pinpointProfile));

        Pair<String, String> profile = ImmutablePair.of(PINPOINT_ACTIVE_PROFILE, pinpointProfile);
        Pair<String, String> log4j2Path = log4j2Path(pinpointProfile);

        Properties properties = merge(profile, log4j2Path);
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            logger.info("PropertiesPropertySource " + entry);
        }

        String sourceName = resolveSourceName(PINPOINT_ACTIVE_PROFILE, pinpointProfile);
        logger.info("Add PropertySource name:" + sourceName);

        PropertiesPropertySource log4j2PathSource = new PropertiesPropertySource(sourceName, properties);

        MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.addLast(log4j2PathSource);
    }

    private String getName() {
        return this.getClass().getSimpleName();
    }


    private Properties merge(Pair<String, String>... pairs) {
        Properties properties = new Properties();
        for (Pair<String, String> pair : pairs) {
            properties.put(pair.getKey(), pair.getValue());
        }
        return properties;
    }

    private String resolveSourceName(String key, String resourcePath) {
        return String.format("%s '%s=%s'", getName(), key, resourcePath);
    }

    private String getDefaultProfile(String[] activeProfiles) {
        if (defaultProfile != null) {
            return defaultProfile;
        }
        return activeProfiles[0];
    }

    private Pair<String, String> log4j2Path(String pinpointActiveProfile) {
        String logConfig = String.format("classpath:profiles/%s/log4j2.xml", pinpointActiveProfile);

        return ImmutablePair.of("logging.config", logConfig);
    }
}
