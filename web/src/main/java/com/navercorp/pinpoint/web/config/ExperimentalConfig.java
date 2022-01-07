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

import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.StreamSupport;

@Component
public class ExperimentalConfig {
    private final Environment environment;

    private Properties experimentalProperties;

    private Map<String, Object> experimentalPropertiesMap;

    private final Logger logger = LogManager.getLogger(ConfigProperties.class);

    public ExperimentalConfig(Environment environment) {
        this.environment = Objects.requireNonNull(environment, "environment");
        this.readExperimentalProperties();
    }

    private void readExperimentalProperties() {
        final String prefix = "experimental.";
        Properties properties = new Properties();
        Map<String, Object> propertiesMap = new HashMap<>();
        MutablePropertySources propertySources = ((AbstractEnvironment) environment).getPropertySources();
        StreamSupport.stream(propertySources.spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                .flatMap(Arrays::<String>stream)
                .filter(propName -> propName.startsWith(prefix))
                .forEach(propName -> {
                    properties.setProperty(propName, environment.getProperty(propName));
                    if (Objects.equals(environment.getProperty(propName), "true") || Objects.equals(environment.getProperty(propName), "false")){
                        propertiesMap.put(propName, Boolean.parseBoolean(environment.getProperty(propName)));
                    } else {
                        propertiesMap.put(propName, environment.getProperty(propName));
                    }
                });
        this.experimentalProperties = properties;
        this.experimentalPropertiesMap = propertiesMap;
    }

    public Map<String, Object> getProperties(){
        return this.experimentalPropertiesMap;
    }

    @PostConstruct
    public void log() {
        logger.info("{}", this);
        AnnotationVisitor<Value> annotationVisitor = new AnnotationVisitor<>(Value.class);
        annotationVisitor.visit(this, new LoggingEvent(this.logger));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ExperimentalConfig{");
        sb.append(this.experimentalProperties.toString());
        sb.append('}');
        return sb.toString();
    }

}