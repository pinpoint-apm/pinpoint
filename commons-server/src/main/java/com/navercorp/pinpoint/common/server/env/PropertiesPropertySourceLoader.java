package com.navercorp.pinpoint.common.server.env;

import com.navercorp.pinpoint.common.server.profile.PinpointProfileEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

public class PropertiesPropertySourceLoader {
    private final Logger logger = LoggerFactory.getLogger(getClass());


    private final ResourceLoader resourceLoader;

    private final String profile;


    public PropertiesPropertySourceLoader(String profile) {
        this(profile, new DefaultResourceLoader());
    }

    public PropertiesPropertySourceLoader(String profile, ResourceLoader resourceLoader) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.resourceLoader = Objects.requireNonNull(resourceLoader, "resourceLoader");
    }

    public PropertiesPropertySource loadPropertySource(String resourceName, List<String> resources) {
        Objects.requireNonNull(resourceName, "resourceName");

        Properties properties = new Properties();
        for (String resourcePath : resources) {
            resourcePath = applyProfilePlaceHolder(resourcePath, profile);

            try {
                Resource resource = resourceLoader.getResource(resourcePath);
                logger.info(resourceName + " fillProperties " + resourcePath);
                fillProperties(properties, resource);
            } catch (IOException ex) {
                throw new IllegalStateException("Failed to load properties from " + resourcePath, ex);
            }
        }
        debugLog(properties);

        return new PropertiesPropertySource(resourceName, properties);

    }

    private void debugLog(Properties properties) {
        String debug = System.getProperty("pinpoint.config.debug", "false");
        if (debug.equalsIgnoreCase("true")) {
            Set<String> names = properties.stringPropertyNames();
            for (String key : names) {
                String value = properties.getProperty(key);
                logger.info(key + "=" + value);
            }
        }
    }

    private void fillProperties(Properties properties, Resource resource) throws IOException {
        EncodedResource encodedResource = new EncodedResource(resource, StandardCharsets.UTF_8);
        PropertiesLoaderUtils.fillProperties(properties, encodedResource);
    }


    private String applyProfilePlaceHolder(String originalResourcePath, String profile) {
        return originalResourcePath.replace(PinpointProfileEnvironment.PROFILE_PLACE_HOLDER, profile);
    }

}

