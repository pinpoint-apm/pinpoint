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
import com.navercorp.pinpoint.bootstrap.util.ProfileConstants;
import com.navercorp.pinpoint.bootstrap.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * @author yjqg6666
 * @author Woonduk Kang(emeroad)
 */
class ProfilePropertyLoader implements PropertyLoader {

    public static final String AGENT_ROOT_PATH_KEY = "pinpoint.agent.root.path";

    private final BootLogger logger = BootLogger.getLogger(getClass());

    private final Properties javaSystemProperty;
    private final Properties osEnvProperty;

    private final Path agentRootPath;
    private final Path profilesPath;

    private final List<Path> supportedProfiles;

    public ProfilePropertyLoader(Properties javaSystemProperty, Properties osEnvProperty, Path agentRootPath, Path profilesPath, List<Path> supportedProfiles) {
        this.javaSystemProperty = Objects.requireNonNull(javaSystemProperty, "javaSystemProperty");
        this.osEnvProperty = Objects.requireNonNull(osEnvProperty, "osEnvProperty");

        this.agentRootPath = Objects.requireNonNull(agentRootPath, "agentRootPath");
        this.profilesPath = Objects.requireNonNull(profilesPath, "profilesPath");
        this.supportedProfiles = Objects.requireNonNull(supportedProfiles, "supportedProfiles");
    }

    /**
     * <pre>Configuration order</pre>
     *
     * <p> Same order as Spring-Boot </p>
     * <p>
     *     <a href="https://docs.spring.io/spring-boot/docs/2.1.9.RELEASE/reference/html/boot-features-external-config.html">
     *         boot-features-external-config
     *     </a>
     * </p>
     * <ol>
     *     <li>Java System properties</li>
     *     <li>OS environment variables</li>
     *     <li>agent external configuration</li>
     *     <li>agent profile configuration /profiles/${profile}/pinpoint.config</li>
     *     <li>agent config /pinpoint-env.config</li>
     * </ol>
     */
    @Override
    public Properties load() {
        final Path defaultConfigPath = this.agentRootPath.resolve(ProfileConstants.CONFIG_FILE_NAME);

        final Properties defaultProperties = new Properties();
        // 1. load default Properties
        logger.info(String.format("load default config:%s", defaultConfigPath));
        defaultProperties.putAll(PropertyLoaderUtils.loadFileProperties(defaultConfigPath));

        // 2. load profile
        final String activeProfile = getActiveProfile(defaultProperties);
        logger.info(String.format("active profile:%s", activeProfile));

        final Path profilePath = Paths.get(profilesPath.toString(), activeProfile, ProfileConstants.PROFILE_CONFIG_FILE_NAME);
        logger.info(String.format("load profile:%s", profilePath));
        defaultProperties.putAll(PropertyLoaderUtils.loadFileProperties(profilePath));

        defaultProperties.setProperty(ProfileConstants.ACTIVE_PROFILE_KEY, activeProfile);

        // 3. load external config
        final String externalConfig = this.javaSystemProperty.getProperty(ProfileConstants.EXTERNAL_CONFIG_KEY);
        if (externalConfig != null) {
            logger.info(String.format("load external config:%s", externalConfig));
            defaultProperties.putAll(PropertyLoaderUtils.loadFileProperties(Paths.get(externalConfig)));
        }

        // 4 OS environment variables
        loadProperties(defaultProperties, this.osEnvProperty);

        // 5. Java System Properties -Dkey=value
        loadProperties(defaultProperties, this.javaSystemProperty);

        // root path
        saveAgentRootPath(agentRootPath, defaultProperties);

        // log path
        return defaultProperties;
    }

    private void saveAgentRootPath(Path agentRootPath, Properties properties) {
        properties.put(AGENT_ROOT_PATH_KEY, agentRootPath);
        logger.info(String.format("agent root path:%s", agentRootPath));
    }


    private String getActiveProfile(Properties defaultProperties) {
        String profile = getProfileProperties(defaultProperties, ProfileConstants.ACTIVE_PROFILE_KEY);
        if (profile == null) {
            throw new RuntimeException("Failed to detect pinpoint profile. Please add -D" +
                    ProfileConstants.ACTIVE_PROFILE_KEY +
                    "=<profile> to VM option. Valid profiles are \"" + supportedProfiles + "\"");
        }

        // prevent directory traversal attack
        for (Path supportedProfile : supportedProfiles) {
            if (supportedProfile.toString().equalsIgnoreCase(profile)) {
                return supportedProfile.toString();
            }
        }

        // handle alias
        for (Path supportedProfile : supportedProfiles) {
            String supportedProfileName = supportedProfile.toString();
            String aliasesStr = getProfileProperties(defaultProperties, ProfileConstants.PROFILE_ALIAS_KEY_PREFIX + supportedProfileName);
            if (containsAlias(aliasesStr, profile)) {
                logger.info(String.format("resolved profile alias '%s' to supported profile '%s'", profile, supportedProfileName));
                return supportedProfileName;
            }
        }
        throw new IllegalStateException("unsupported profile:" + profile);
    }

    private String getProfileProperties(Properties defaultProperties, String key) {
//        env option support??
//        String envProfile = System.getenv(ACTIVE_PROFILE_KEY);
        String value = javaSystemProperty.getProperty(key);
        if (value == null) {
            value = defaultProperties.getProperty(key);
        }
        return value;
    }


    private void loadProperties(Properties dstProperties, Properties property) {
        Map<Object, Object> copy = PropertyLoaderUtils.filterAllowedPrefix(property);
        dstProperties.putAll(copy);
    }

    private boolean containsAlias(String aliasesStr, String profile) {
        if (StringUtils.isEmpty(aliasesStr)) {
            return false;
        }

        String[] aliases = aliasesStr.split(",");
        for (String alias : aliases) {
            if (profile.equalsIgnoreCase(alias.trim())) {
                return true;
            }
        }
        return false;
    }
}
