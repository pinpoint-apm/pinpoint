package com.navercorp.pinpoint.common.server.profile;

import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

public class ProfileEnvironment {
    public static final String PINPOINT_ACTIVE_PROFILE = "pinpoint.profiles.active";
    public static final String PROFILE_PLACE_HOLDER = "${" + PINPOINT_ACTIVE_PROFILE + "}";

    private final ServerBootLogger logger = ServerBootLogger.getLogger(getClass());

    public void processEnvironment(ConfigurableEnvironment environment) {
        String[] activeProfiles = environment.getActiveProfiles();

        logger.info(org.springframework.core.env.AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME + ":" + Arrays.toString(activeProfiles));

        final String pinpointProfile = getDefaultProfile(activeProfiles);
        logger.info(PINPOINT_ACTIVE_PROFILE + ":" + pinpointProfile);

        Pair<String, String> profile = ImmutablePair.of(PINPOINT_ACTIVE_PROFILE, pinpointProfile);
        Pair<String, String> log4j2Path = log4j2Path(pinpointProfile);

        Properties properties = merge(profile, log4j2Path);
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            logger.info("PropertiesPropertySource " + entry);
        }
        MutablePropertySources propertySources = environment.getPropertySources();
        logger.info("Add PropertySource name:" + getName());
        PropertiesPropertySource log4j2PathSource = new PropertiesPropertySource(getName(), properties);
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


    private String getDefaultProfile(String[] activeProfiles) {
        // TODO
        return activeProfiles[0];
    }

    private Pair<String, String> log4j2Path(String pinpointActiveProfile) {
        String logConfig = String.format("classpath:profiles/%s/log4j2.xml", pinpointActiveProfile);

        return ImmutablePair.of("logging.config", logConfig);
    }
}
