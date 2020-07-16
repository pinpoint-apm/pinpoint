/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.profile;

import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ProfileApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {
    public static final String PINPOINT_ACTIVE_PROFILE = "pinpoint.profiles.active";

    private final ServerBootLogger logger = ServerBootLogger.getLogger(getClass());


    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {

        logger.info("onApplicationEvent-ApplicationEnvironmentPreparedEvent");

        ConfigurableEnvironment environment = event.getEnvironment();
        String[] activeProfiles = environment.getActiveProfiles();

        logger.info(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME + ":" + Arrays.toString(activeProfiles));

        final String pinpointProfile = getDefaultProfile(activeProfiles);
        logger.info(PINPOINT_ACTIVE_PROFILE + ":" + pinpointProfile);

        Pair<String, String> profile = ImmutablePair.of(PINPOINT_ACTIVE_PROFILE, pinpointProfile);
        Pair<String, String> log4j2Path = log4j2Path(pinpointProfile);

        Properties properties = merge(profile, log4j2Path);
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            logger.info("PropertiesPropertySource " + entry);
        }
        MutablePropertySources propertySources = environment.getPropertySources();
        PropertiesPropertySource log4j2PathSource = new PropertiesPropertySource("PinpointProfileSource", properties);
        propertySources.addLast(log4j2PathSource);
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

    /**
     * @see org.springframework.boot.context.logging.LoggingApplicationListener#DEFAULT_ORDER
     * @see ConfigFileApplicationListener#DEFAULT_ORDER
     */
    @Override
    public int getOrder() {
        return ConfigFileApplicationListener.DEFAULT_ORDER + 1;
    }


}
