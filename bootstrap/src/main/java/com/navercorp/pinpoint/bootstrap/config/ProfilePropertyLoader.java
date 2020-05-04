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
import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.util.PropertyUtils;
import com.navercorp.pinpoint.common.util.SimpleProperty;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * @author yjqg6666
 * @author Woonduk Kang(emeroad)
 */
class ProfilePropertyLoader implements PropertyLoader {

    private static final String SEPARATOR = File.separator;

    private final BootLogger logger = BootLogger.getLogger(ProfilePropertyLoader.class.getName());
    private final SimpleProperty systemProperty;

    private final String agentRootPath;
    private final String profilesPath;

    private final String[] supportedProfiles;

    public static final String[] ALLOWED_PROPERTY_PREFIX = new String[]{"bytecode.", "profiler.", "pinpoint."};

    public ProfilePropertyLoader(SimpleProperty systemProperty, String agentRootPath, String profilesPath, String[] supportedProfiles) {
        this.systemProperty = Assert.requireNonNull(systemProperty, "systemProperty");
        this.agentRootPath = Assert.requireNonNull(agentRootPath, "agentRootPath");
        this.profilesPath = Assert.requireNonNull(profilesPath, "profilesPath");
        this.supportedProfiles = Assert.requireNonNull(supportedProfiles, "supportedProfiles");
    }

    @Override
    public Properties load() {
        final String defaultConfigPath = this.agentRootPath + SEPARATOR + Profiles.CONFIG_FILE_NAME;

        final Properties defaultProperties = new Properties();
        // 1. load default Properties
        logger.info(String.format("load default config:%s", defaultConfigPath));
        loadFileProperties(defaultProperties, defaultConfigPath);

        // 2. load profile
        final String activeProfile = getActiveProfile(defaultProperties);
        logger.info(String.format("active profile:%s", activeProfile));
        if (activeProfile != null) {
            final String profilePath = this.profilesPath + SEPARATOR + activeProfile + SEPARATOR + Profiles.PROFILE_CONFIG_FILE_NAME;
            logger.info(String.format("load profile:%s", profilePath));
            loadFileProperties(defaultProperties, profilePath);

            defaultProperties.setProperty(Profiles.ACTIVE_PROFILE_KEY, activeProfile);
        }

        // 3. load external config
        final String externalConfig = this.systemProperty.getProperty(Profiles.EXTERNAL_CONFIG_KEY);
        if (externalConfig != null) {
            logger.info(String.format("load external config:%s", externalConfig));
            loadFileProperties(defaultProperties, externalConfig);
        }
        // ?? 4. systemproperty -Dkey=value?
        loadSystemProperties(defaultProperties);

        // log path
        saveLogConfigLocation(activeProfile, defaultProperties);
        return defaultProperties;
    }

    private void saveLogConfigLocation(String activeProfile, Properties properties) {
        LogConfigResolver logConfigResolver = new ProfileLogConfigResolver(profilesPath, activeProfile);
        final String log4jLocation = logConfigResolver.getLogPath();

        properties.put(Profiles.LOG_CONFIG_LOCATION_KEY, log4jLocation);
        logger.info(String.format("logConfig path:%s", log4jLocation));
    }

    private String getActiveProfile(Properties defaultProperties) {
//        env option support??
//        String envProfile = System.getenv(ACTIVE_PROFILE_KEY);
        String profile = systemProperty.getProperty(Profiles.ACTIVE_PROFILE_KEY);
        if (profile == null) {
            profile = defaultProperties.getProperty(Profiles.ACTIVE_PROFILE_KEY, Profiles.DEFAULT_ACTIVE_PROFILE);
        }
        if (profile == null) {
            // empty profile
            return null;
        }

        // prevent directory traversal attack
        for (String supportedProfile : supportedProfiles) {
            if (supportedProfile.equalsIgnoreCase(profile)) {
                return supportedProfile;
            }
        }
        throw new IllegalStateException("unsupported profile:" + profile);
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
            if (isAllowPinpointProperty(propertyName)) {
                String val = this.systemProperty.getProperty(propertyName);
                dstProperties.setProperty(propertyName, val);
            }
        }
    }

    @VisibleForTesting
    boolean isAllowPinpointProperty(String propertyName) {
        for (String prefix : ALLOWED_PROPERTY_PREFIX) {
            if (propertyName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

}
