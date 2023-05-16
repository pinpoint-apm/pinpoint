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

import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExperimentalProperties {
    public static final String PREFIX = "experimental.";

    private final Map<String, Object> properties;


    public ExperimentalProperties(Map<String, Object> properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public static ExperimentalProperties of(Environment environment) {

        MutablePropertySources propertySources = ((AbstractEnvironment) environment).getPropertySources();
        Map<String, Object> collect = propertySources.stream()
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                .flatMap(Arrays::stream)
                .filter(propName -> propName.startsWith(PREFIX))
                .collect(Collectors.toMap(Function.identity(), toValue(environment)));
        
        return new ExperimentalProperties(collect);
    }

    private static Function<String, Object> toValue(Environment environment) {
        return key -> {
            final String value = environment.getProperty(key);
            final Boolean boolValue = parseBoolean(value);
            if (boolValue != null) {
                return boolValue;
            }

            return value;
        };
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