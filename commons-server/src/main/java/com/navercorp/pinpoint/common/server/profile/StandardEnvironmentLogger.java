package com.navercorp.pinpoint.common.server.profile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class StandardEnvironmentLogger implements InitializingBean {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final StandardEnvironment env;

    public StandardEnvironmentLogger(StandardEnvironment env) {
        this.env = Objects.requireNonNull(env, "env");

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MutablePropertySources propertySources = env.getPropertySources();
        for (org.springframework.core.env.PropertySource<?> propertySource : propertySources) {
            logger.info(propertySource);
        }

//        for (org.springframework.core.env.PropertySource<?> propertySource : propertySources) {
//            logger.info(propertySource.getName() + " " + propertySource.getSource());
//        }
    }
}
