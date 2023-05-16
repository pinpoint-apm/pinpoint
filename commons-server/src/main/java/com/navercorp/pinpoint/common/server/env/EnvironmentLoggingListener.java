/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.env;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

public class EnvironmentLoggingListener implements ApplicationListener<SpringApplicationEvent> {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public void onApplicationEvent(SpringApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            ApplicationEnvironmentPreparedEvent prepared = (ApplicationEnvironmentPreparedEvent) event;
            ConfigurableEnvironment environment = prepared.getEnvironment();

            logPropertySource(event, environment);
        } else if (event instanceof ApplicationStartedEvent) {
            ApplicationStartedEvent started = (ApplicationStartedEvent) event;
            ConfigurableEnvironment environment = started.getApplicationContext().getEnvironment();

            logPropertySource(event, environment);
        } else if (event instanceof ApplicationFailedEvent) {
            ApplicationFailedEvent failed = (ApplicationFailedEvent) event;
            ConfigurableEnvironment environment = failed.getApplicationContext().getEnvironment();

            logPropertySource(event, environment);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("ApplicationEvent:{}", event.getClass().getSimpleName());
            }
        }
    }

    private void logPropertySource(SpringApplicationEvent event, ConfigurableEnvironment environment) {
        logger.info("applicationEvent:{}", event.getClass().getSimpleName());

        MutablePropertySources propertySources = environment.getPropertySources();

        for (PropertySource<?> propertySource : propertySources) {
            if (propertySource instanceof CompositePropertySource) {
                CompositePropertySource cps = (CompositePropertySource) propertySource;
                logger.info("CompositePropertySource name {}", cps.getName());
                for (PropertySource<?> child : cps.getPropertySources()) {
                    logger.info("  {}", child.getName());
                }
            } else {
                logger.info("PropertySource name {}", propertySource.getName());
            }
        }
    }
}
