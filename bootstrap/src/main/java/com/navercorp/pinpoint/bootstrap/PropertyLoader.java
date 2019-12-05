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

package com.navercorp.pinpoint.bootstrap;

import com.navercorp.pinpoint.bootstrap.config.Profiles;
import com.navercorp.pinpoint.common.util.PropertyUtils;
import com.navercorp.pinpoint.common.util.SimpleProperty;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PropertyLoader {

    private static final String SEPARATOR = File.separator;

    private final BootLogger logger = BootLogger.getLogger(PinpointStarter.class.getName());
    private final SimpleProperty systemProperty;

    private final String agentRootPath;
    private final String profilesPath;

    private final String[] supportedProfiles;

    public PropertyLoader(SimpleProperty systemProperty, String agentRootPath, String profilesPath, String[] supportedProfiles) {
        if (systemProperty == null) {
            throw new NullPointerException("systemProperty");
        }
        if (agentRootPath == null) {
            throw new NullPointerException("agentRootPath");
        }
        if (profilesPath == null) {
            throw new NullPointerException("profilesPath");
        }
        if (supportedProfiles == null) {
            throw new NullPointerException("supportedProfiles");
        }
        this.systemProperty = systemProperty;
        this.agentRootPath = agentRootPath;
        this.profilesPath = profilesPath;
        this.supportedProfiles = supportedProfiles;
    }

    public Properties load() {
        final String defaultConfigPath = this.agentRootPath + SEPARATOR + Profiles.CONFIG_FILE_NAME;
        Properties defaultProperties = new Properties();
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
        return defaultProperties;
    }

    private String getActiveProfile(Properties defaultProperties) {
//        env option support??
//        String envProfile = System.getenv(ACTIVE_PROFILE_KEY);
        String profile = systemProperty.getProperty(Profiles.ACTIVE_PROFILE_KEY);
        if (profile == null) {
            profile = defaultProperties.getProperty(Profiles.ACTIVE_PROFILE_KEY, Profiles.DEFAULT_ACTIVE_PROFILE);
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

}
