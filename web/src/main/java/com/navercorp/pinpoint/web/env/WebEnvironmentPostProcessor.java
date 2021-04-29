package com.navercorp.pinpoint.web.env;

import com.navercorp.pinpoint.common.server.env.BaseEnvironment;
import com.navercorp.pinpoint.common.server.env.ExternalEnvironment;
import com.navercorp.pinpoint.common.server.profile.ProfileEnvironment;
import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.Arrays;

public class WebEnvironmentPostProcessor implements EnvironmentPostProcessor {
    private final ServerBootLogger logger = ServerBootLogger.getLogger(getClass());

    public static final String PROPERTY_SOURCE_NAME = "WebEnvironment";
    public static final String EXTERNAL_PROPERTY_SOURCE_NAME = "WebExternalEnvironment";

    private final String[] resources = new String[] {
            "classpath:hbase-root.properties",
            "classpath:jdbc-root.properties",
            "classpath:pinpoint-web-root.properties",
            "classpath:batch-root.properties",
//            <!-- override configuration -->
            "classpath:profiles/${pinpoint.profiles.active}/hbase.properties",
            "classpath:profiles/${pinpoint.profiles.active}/jdbc.properties",
            "classpath:profiles/${pinpoint.profiles.active}/pinpoint-web.properties",
            "classpath:profiles/${pinpoint.profiles.active}/batch.properties",
    };
    private static final String EXTERNAL_CONFIGURATION_KEY = "pinpoint.web.config.location";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        logger.info("postProcessEnvironment");

        ProfileEnvironment profileEnvironment = new ProfileEnvironment();
        profileEnvironment.processEnvironment(environment);

        ExternalEnvironment externalEnvironment
                = new ExternalEnvironment(EXTERNAL_PROPERTY_SOURCE_NAME, EXTERNAL_CONFIGURATION_KEY);
        externalEnvironment.processEnvironment(environment);

        BaseEnvironment baseEnvironment
                = new BaseEnvironment(PROPERTY_SOURCE_NAME, Arrays.asList(resources));
        baseEnvironment.processEnvironment(environment);

        MutablePropertySources propertySources = environment.getPropertySources();
        for (PropertySource<?> propertySource : propertySources) {
            logger.info("Environment order " + propertySource.getName());
        }

    }

}
