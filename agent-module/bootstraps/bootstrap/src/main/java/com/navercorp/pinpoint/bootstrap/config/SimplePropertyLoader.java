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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
class SimplePropertyLoader implements PropertyLoader {

//    private static final String SEPARATOR = File.separator;

    private final BootLogger logger = BootLogger.getLogger(this.getClass());
    private final Properties systemProperty;

    private final Path agentRootPath;


    public SimplePropertyLoader(Properties systemProperty, Path agentRootPath) {
        this.systemProperty = Objects.requireNonNull(systemProperty, "systemProperty");
        this.agentRootPath = Objects.requireNonNull(agentRootPath, "agentRootPath");
    }

    @Override
    public Properties load() {
        final Path defaultConfigPath = this.agentRootPath.resolve(ProfileConstants.CONFIG_FILE_NAME);

        final Properties defaultProperties = new Properties();

        final String externalConfig = this.systemProperty.getProperty(ProfileConstants.EXTERNAL_CONFIG_KEY);
        if (externalConfig != null) {
            logger.info(String.format("load external config:%s", externalConfig));
            defaultProperties.putAll(PropertyLoaderUtils.loadFileProperties(Paths.get(externalConfig)));
        } else {
            logger.info(String.format("load default config:%s", defaultConfigPath));
            defaultProperties.putAll(PropertyLoaderUtils.loadFileProperties(defaultConfigPath));
        }
        // systemProperty
        loadProperties(defaultProperties, this.systemProperty);
        return defaultProperties;
    }



    private void loadProperties(Properties dstProperties, Properties property) {
        Map<Object, Object> copy = PropertyLoaderUtils.filterAllowedPrefix(property);
        dstProperties.putAll(copy);
    }

}
