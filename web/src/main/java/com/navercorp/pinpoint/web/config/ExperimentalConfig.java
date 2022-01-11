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

package com.navercorp.pinpoint.web.config;

import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ExperimentalConfig {
    public static final String PREFIX = "experimental.";

    private final Map<String, Object> properties;


    public ExperimentalConfig(Environment environment) {
        Objects.requireNonNull(environment, "environment");
        this.properties = readExperimentalProperties(environment);
    }

    private Map<String, Object> readExperimentalProperties(Environment environment) {

        MutablePropertySources propertySources = ((AbstractEnvironment) environment).getPropertySources();
        Map<String, Object> collect = propertySources.stream()
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                .flatMap(Arrays::stream)
                .filter(propName -> propName.startsWith(PREFIX))
                .collect(Collectors.toMap(Function.identity(), toValue(environment)));
        return collect;
    }

    private Function<String, Object> toValue(Environment environment) {
        return key -> {
            final String value = environment.getProperty(key);
            final Boolean boolValue = parseBoolean(value);
            if (boolValue != null) {
                return boolValue;
            }

            return value;
        };
    }

    private Boolean parseBoolean(String value) {
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