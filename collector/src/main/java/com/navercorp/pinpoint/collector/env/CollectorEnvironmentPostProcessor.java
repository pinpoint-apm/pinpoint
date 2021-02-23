package com.navercorp.pinpoint.collector.env;

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

public class CollectorEnvironmentPostProcessor implements EnvironmentPostProcessor {
    private final ServerBootLogger logger = ServerBootLogger.getLogger(getClass());

    public static final String COLLECTOR_PROPERTY_SOURCE_NAME = "CollectorEnvironment";
    public static final String COLLECTOR_EXTERNAL_PROPERTY_SOURCE_NAME = "CollectorExternalEnvironment";

    private final String[] resources = new String[] {
            "classpath:hbase-root.properties",
            "classpath:pinpoint-collector-root.properties",
            "classpath:pinpoint-collector-grpc-root.properties",
            "classpath:jdbc-root.properties",
//            <!-- override configuration -->
            "classpath:profiles/${pinpoint.profiles.active}/hbase.properties",
            "classpath:profiles/${pinpoint.profiles.active}/pinpoint-collector.properties",
            "classpath:profiles/${pinpoint.profiles.active}/pinpoint-collector-grpc.properties",
            "classpath:profiles/${pinpoint.profiles.active}/jdbc.properties",
    };
    private static final String EXTERNAL_CONFIGURATION_KEY = "pinpoint.collector.config.location";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        logger.info("postProcessEnvironment");
        application.setLogStartupInfo(true);

        ProfileEnvironment profileEnvironment = new ProfileEnvironment();
        profileEnvironment.processEnvironment(environment);

        ExternalEnvironment externalEnvironment
                = new ExternalEnvironment(COLLECTOR_EXTERNAL_PROPERTY_SOURCE_NAME, EXTERNAL_CONFIGURATION_KEY);
        externalEnvironment.processEnvironment(environment);

        BaseEnvironment baseEnvironment
                = new BaseEnvironment(COLLECTOR_PROPERTY_SOURCE_NAME, Arrays.asList(resources));
        baseEnvironment.processEnvironment(environment);

        MutablePropertySources propertySources = environment.getPropertySources();
        for (PropertySource<?> propertySource : propertySources) {
            logger.info("Environment order " + propertySource.getName());
        }

    }

}
