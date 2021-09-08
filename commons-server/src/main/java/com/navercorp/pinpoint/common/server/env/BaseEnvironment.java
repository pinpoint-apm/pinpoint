package com.navercorp.pinpoint.common.server.env;

import com.navercorp.pinpoint.common.server.profile.PinpointProfileEnvironment;
import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.util.List;
import java.util.Objects;

public class BaseEnvironment {
    private final ServerBootLogger logger = ServerBootLogger.getLogger(getClass());

    private final String name;
    private final List<String> resources;

    private ResourceLoader resourceLoader;

    public BaseEnvironment(String name, List<String> resources) {
        this.name = Objects.requireNonNull(name, "name");
        this.resources = Objects.requireNonNull(resources, "resources");
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = Objects.requireNonNull(resourceLoader, "resourceLoader");
    }

    public void processEnvironment(ConfigurableEnvironment environment) {
        final String profile = environment.getProperty(PinpointProfileEnvironment.PINPOINT_ACTIVE_PROFILE);
        if (profile == null) {
            throw new IllegalStateException("profile is not set");
        }
        final ResourceLoader resourceLoader = defaultResourceLoader();
        logger.info("load PropertySource name:" + getName());

        PropertiesPropertySourceLoader loader = new PropertiesPropertySourceLoader(profile, resourceLoader);
        PropertiesPropertySource propertySource = loader.loadPropertySource(getName(), resources);

        logger.info("Add PropertySource name:" + getName());
        environment.getPropertySources().addLast(propertySource);
    }

    public String getName() {
        return name;
    }

    private ResourceLoader defaultResourceLoader() {
        if (this.resourceLoader == null) {
            this.resourceLoader = new DefaultResourceLoader();
        }
        return this.resourceLoader;
    }


}
