package com.navercorp.pinpoint.common.server.env;

import com.navercorp.pinpoint.common.server.profile.PinpointProfileEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class ExternalEnvironment {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String name;
    private final String externalConfigurationKey;
    private ResourceLoader resourceLoader;

    public ExternalEnvironment(String name, String externalConfigurationKey) {
        this.name = Objects.requireNonNull(name, "name");
        this.externalConfigurationKey = Objects.requireNonNull(externalConfigurationKey, "externalConfigurationKey");
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = Objects.requireNonNull(resourceLoader, "resourceLoader");
    }

    public void processEnvironment(ConfigurableEnvironment environment) {
        final String profile = environment.getProperty(PinpointProfileEnvironment.PINPOINT_ACTIVE_PROFILE);
        if (profile == null) {
            throw new IllegalStateException("profile is not set");
        }

        ResourceLoader resourceLoader = defaultResourceLoader();
        Map<String, Object> systemEnvironment = environment.getSystemProperties();
        final String externalConfigLocation = getString(systemEnvironment, externalConfigurationKey);
        if (externalConfigLocation == null) {
            logger.info(String.format("-D%s is not set", externalConfigurationKey));
            return;
        }

        logger.info("load PropertySource name:{}", getName());
        PropertiesPropertySourceLoader loader = new PropertiesPropertySourceLoader(profile, resourceLoader);

        String sourceName = resolveSourceName(externalConfigurationKey, externalConfigLocation);
        String resourcePath = String.format("file:%s", externalConfigLocation);
        PropertiesPropertySource propertySource = loader.loadPropertySource(sourceName, Collections.singletonList(resourcePath));

        logger.info("Add PropertySource name:{}", sourceName);
        environment.getPropertySources().addLast(propertySource);
    }

    private String resolveSourceName(String key, String resourcePath) {
        return String.format("%s '%s=%s'", getName(), key, resourcePath);
    }

    private ResourceLoader defaultResourceLoader() {
        if (this.resourceLoader == null) {
            this.resourceLoader = new DefaultResourceLoader();
        }
        return this.resourceLoader;
    }

    private String getString(Map<String, Object> systemEnvironment, String key) {
        final Object value = systemEnvironment.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    public String getName() {
        return name;
    }
}