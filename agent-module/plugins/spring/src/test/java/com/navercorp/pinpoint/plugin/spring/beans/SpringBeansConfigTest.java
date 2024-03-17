/*
 * Copyright 2016 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.spring.beans;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfigLoader;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jaehong.kim
 */
public class SpringBeansConfigTest {
    @Test
    public void config() {
        Properties properties = new Properties();
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "Target.*");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Controller");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "java.lang.String");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Service");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 3 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Repository");
        ProfilerConfig config = ProfilerConfigLoader.load(properties);

        SpringBeansConfig springBeansConfig = new SpringBeansConfig(config);
        assertThat(springBeansConfig.getTargets()).hasSize(3);

        SpringBeansTarget target1 = springBeansConfig.getTarget(1);
        assertEquals(SpringBeansTargetScope.COMPONENT_SCAN, target1.getScope());
        assertThat(target1.getBasePackages()).isNull();
        assertThat(target1.getNamePatterns()).hasSize(1);
        assertThat(target1.getAnnotations()).hasSize(1);
        assertThat(target1.getClassPatterns()).isNull();

        SpringBeansTarget target2 = springBeansConfig.getTarget(2);
        assertEquals(SpringBeansTargetScope.COMPONENT_SCAN, target2.getScope());
        assertThat(target2.getBasePackages()).isNull();
        assertThat(target2.getNamePatterns()).isNull();
        assertThat(target2.getAnnotations()).hasSize(1);
        assertThat(target2.getClassPatterns()).hasSize(1);

        SpringBeansTarget target3 = springBeansConfig.getTarget(3);
        assertEquals(SpringBeansTargetScope.COMPONENT_SCAN, target3.getScope());
        assertThat(target3.getBasePackages()).isNull();
        assertThat(target3.getNamePatterns()).isNull();
        assertThat(target3.getAnnotations()).hasSize(1);
        assertThat(target3.getClassPatterns()).isNull();
    }

    @Test
    public void backwardCompatibility() {
        Properties properties = new Properties();
        properties.put(SpringBeansConfig.SPRING_BEANS_NAME_PATTERN, "");
        properties.put(SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN, "com.navercorp.*");
        properties.put(SpringBeansConfig.SPRING_BEANS_ANNOTATION, "org.springframework.stereotype.Controller, org.springframework.stereotype.Servicem, org.springframework.stereotype.Repository");

        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "Target.*");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Controller");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "java.lang.String");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Service");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 3 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Repository");
        ProfilerConfig config = ProfilerConfigLoader.load(properties);

        SpringBeansConfig springBeansConfig = new SpringBeansConfig(config);

        // backward compatiblity.
        assertThat(springBeansConfig.getTargets()).hasSize(5);
    }

    @Test
    public void empty() {
        Properties properties = new Properties();
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "Target.*");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Controller");
        // empty
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "");
        // empty
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 3 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "");
        // empty
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 4 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 4 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "");
        // empty
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 5 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 5 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "");

        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 6 + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "java.lang.String");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 6 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Service");

        ProfilerConfig config = ProfilerConfigLoader.load(properties);

        SpringBeansConfig springBeansConfig = new SpringBeansConfig(config);
        assertThat(springBeansConfig.getTargets()).hasSize(2);
    }

    @Test
    public void pattern() {
        Properties properties = new Properties();
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "Target.*");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Controller");

        // normal
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 6 + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "java.lang.String");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 6 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Service");

        // old
        properties.put(SpringBeansConfig.SPRING_BEANS_NAME_PATTERN, "com.navercorp.*");

        ProfilerConfig config = ProfilerConfigLoader.load(properties);
        SpringBeansConfig springBeansConfig = new SpringBeansConfig(config);
        assertThat(springBeansConfig.getTargets()).hasSize(3);
    }

    @Test
    public void invalid() {
        Properties properties = new Properties();
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + "foo", "Target.*");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + "bar", "org.springframework.stereotype.Controller");
        // empty
        properties.put("foo" + 2 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "");
        properties.put("bar" + 2 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "");

        // wrong number 1.
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 6.12 + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "java.lang.String");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 6.12 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Service");

        // wrong number 2.
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + "A" + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "java.lang.String");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + "A" + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Service");

        // not found number
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "java.lang.String");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Service");

        ProfilerConfig config = ProfilerConfigLoader.load(properties);
        SpringBeansConfig springBeansConfig = new SpringBeansConfig(config);
        assertThat(springBeansConfig.getTargets()).isEmpty();
    }

    @Test
    public void scope() {
        Properties properties = new Properties();

        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_SCOPE_POSTFIX, "component-scan");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Service");

        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_SCOPE_POSTFIX, "component-scan");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_BASE_PACKAGES_POSTFIX, "com.navercorp");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Service");

        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 3 + SpringBeansConfig.SPRING_BEANS_SCOPE_POSTFIX, "post-processor");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 3 + SpringBeansConfig.SPRING_BEANS_BASE_PACKAGES_POSTFIX, "com.navercorp");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 3 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Service");

        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 4 + SpringBeansConfig.SPRING_BEANS_SCOPE_POSTFIX, "post-processor");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 4 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Service");

        ProfilerConfig config = ProfilerConfigLoader.load(properties);
        SpringBeansConfig springBeansConfig = new SpringBeansConfig(config);
        assertThat(springBeansConfig.getTargets()).hasSize(4);
    }
}