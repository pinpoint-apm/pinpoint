/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.config;

import com.navercorp.pinpoint.bootstrap.BootLogger;
import com.navercorp.pinpoint.bootstrap.agentdir.Assert;
import com.navercorp.pinpoint.common.util.PropertyUtils;
import com.navercorp.pinpoint.common.util.SimpleProperty;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * @author Woonduk Kang(emeroad)
 */
class SimplePropertyLoader implements PropertyLoader {

    private static final String SEPARATOR = File.separator;

    private final BootLogger logger = BootLogger.getLogger(SimplePropertyLoader.class.getName());
    private final SimpleProperty systemProperty;

    private final String agentRootPath;
    private final String profilesPath;


    public SimplePropertyLoader(SimpleProperty systemProperty, String agentRootPath, String profilesPath) {
        this.systemProperty = Assert.requireNonNull(systemProperty, "systemProperty");
        this.agentRootPath = Assert.requireNonNull(agentRootPath, "agentRootPath");
        this.profilesPath = profilesPath;
    }

    @Override
    public Properties load() {
        final String defaultConfigPath = this.agentRootPath + SEPARATOR + Profiles.CONFIG_FILE_NAME;

        final Properties defaultProperties = new Properties();

        final String externalConfig = this.systemProperty.getProperty(Profiles.EXTERNAL_CONFIG_KEY);
        if (externalConfig != null) {
            logger.info(String.format("load external config:%s", externalConfig));
            loadFileProperties(defaultProperties, externalConfig);
        } else {
            logger.info(String.format("load default config:%s", defaultConfigPath));
            loadFileProperties(defaultProperties, defaultConfigPath);
        }
        loadSystemProperties(defaultProperties);
        saveLogConfigLocation(defaultProperties);
        return defaultProperties;
    }


    private void saveLogConfigLocation(Properties properties) {
        String activeProfile = systemProperty.getProperty(Profiles.ACTIVE_PROFILE_KEY, Profiles.DEFAULT_ACTIVE_PROFILE);
        LogConfigResolver logConfigResolver = new ProfileLogConfigResolver(profilesPath, activeProfile);
        final String log4jLocation = logConfigResolver.getLogPath();

        properties.put(Profiles.LOG_CONFIG_LOCATION_KEY, log4jLocation);
        logger.info(String.format("logConfig path:%s", log4jLocation));
    }



    private void loadFileProperties(Properties properties, String filePath) {
        try {
            PropertyUtils.FileInputStreamFactory fileInputStreamFactory = new PropertyUtils.FileInputStreamFactory(filePath);
            PropertyUtils.loadProperty(properties, fileInputStreamFactory, PropertyUtils.DEFAULT_ENCODING);
        } catch (IOException e) {
            logger.info(String.format("%s load fail Caused by:%s", filePath, e.getMessage()));
            throw new IllegalStateException(String.format("%s load fail Caused by:%s", filePath, e.getMessage()));
        }
    }

    private void loadSystemProperties(Properties dstProperties) {
        Set<String> stringPropertyNames = this.systemProperty.stringPropertyNames();
        for (String propertyName : stringPropertyNames) {
            boolean isPinpointProperty = propertyName.startsWith("bytecode.") || propertyName.startsWith("profiler.") || propertyName.startsWith("pinpoint.");
            if (isPinpointProperty) {
                String val = this.systemProperty.getProperty(propertyName);
                dstProperties.setProperty(propertyName, val);
            }
        }
    }

}
