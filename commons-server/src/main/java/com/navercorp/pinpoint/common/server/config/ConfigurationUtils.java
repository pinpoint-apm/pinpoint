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

package com.navercorp.pinpoint.common.server.config;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class ConfigurationUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationUtils.class);

    private ConfigurationUtils() {
    }

    public static String readString(Properties properties, String propertyName, String defaultValue) {
        final String result = properties.getProperty(propertyName, defaultValue);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{}={}", propertyName, result);
        }
        return result ;
    }

    public static int readInt(Properties properties, String propertyName, int defaultValue) {
        final String value = properties.getProperty(propertyName);
        final int result = NumberUtils.toInt(value, defaultValue);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{}={}", propertyName, result);
        }
        return result;
    }

    public static long readLong(Properties properties, String propertyName, long defaultValue) {
        final String value = properties.getProperty(propertyName);
        final long result = NumberUtils.toLong(value, defaultValue);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{}={}", propertyName, result);
        }
        return result;
    }


    public static boolean readBoolean(Properties properties, String propertyName) {
        final String value = properties.getProperty(propertyName);

        // if a default value will be needed afterwards, may match string value instead of Utils.
        // for now stay unmodified because of no need.

        final boolean result = Boolean.valueOf(value);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{}={}", propertyName, result);
        }
        return result;
    }
}
