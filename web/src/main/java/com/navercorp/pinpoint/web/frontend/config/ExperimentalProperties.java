/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.frontend.config;

import com.navercorp.pinpoint.common.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ExperimentalProperties {
    private static final Logger logger = LogManager.getLogger(ExperimentalProperties.class);

    public static final String PREFIX = "experimental.";

    private final Map<String, Object> properties;


    public ExperimentalProperties(Map<String, Object> properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public static ExperimentalProperties of(Environment environment) {

        Map<String, Object> map = new LinkedHashMap<>();
        for (PropertySource<?> ps : ((ConfigurableEnvironment) environment).getPropertySources()) {
            if (ps instanceof EnumerablePropertySource<?> source) {
                for (String name : source.getPropertyNames()) {
                    if (name.startsWith(PREFIX)) {
                        Object value = getValue(environment, name);
                        map.putIfAbsent(name, value);
                    }
                }
            }
        }
        return new ExperimentalProperties(map);
    }

    private static Object getValue(Environment environment, String key) {
        final String value = environment.getProperty(key);
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        final Boolean boolValue = parseBoolean(value);
        if (boolValue != null) {
            return boolValue;
        }
        return value;
    }

    private static Boolean parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        }
        return null;
    }


    public Map<String, Object> getProperties() {
        return this.properties;
    }


    @Override
    public String toString() {
        return "ExperimentalConfig{" +
                ", experimentalProperties=" + properties +
                '}';
    }
}