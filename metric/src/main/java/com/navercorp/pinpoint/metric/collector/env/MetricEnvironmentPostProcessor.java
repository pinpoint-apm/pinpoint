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

package com.navercorp.pinpoint.metric.collector.env;

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

/**
 * @author minwoo.jung
 */
public class MetricEnvironmentPostProcessor implements EnvironmentPostProcessor {
    private final ServerBootLogger logger = ServerBootLogger.getLogger(getClass());

    public static final String COLLECTOR_PROPERTY_SOURCE_NAME = "CollectorEnvironment";
    public static final String COLLECTOR_EXTERNAL_PROPERTY_SOURCE_NAME = "CollectorExternalEnvironment";

    private final String[] resources = new String[] {
            "classpath:pinot-collector/profiles/${pinpoint.profiles.active}/jdbc.properties",
    };
    private static final String EXTERNAL_CONFIGURATION_KEY = "pinpoint.collector.config.location";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        logger.info("postProcessEnvironment");

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
