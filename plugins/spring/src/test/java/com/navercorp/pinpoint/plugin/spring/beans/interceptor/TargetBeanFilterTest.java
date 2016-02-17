/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.spring.beans.interceptor;

import static org.junit.Assert.*;

import java.util.Properties;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.plugin.spring.beans.SpringBeansConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jongho Moon
 * @author jaehong.kim
 */
public class TargetBeanFilterTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testClassLoadedByBootClassLoader() {
        Properties properties = new Properties();
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Controller");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Service");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 3 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Repository");
        ProfilerConfig config = new DefaultProfilerConfig(properties);
        TargetBeanFilter filter = TargetBeanFilter.of(config);

        if (String.class.getClassLoader() != null) {
            logger.debug("String is not loaded by: {}. Skip test.", String.class.getClassLoader());
            return;
        }

        // should not throw an exception
        filter.isTarget("someBean", String.class);
    }

    @Test
    public void empty() {
        Properties properties = new Properties();
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "");
        ProfilerConfig config = new DefaultProfilerConfig(properties);
        TargetBeanFilter filter = TargetBeanFilter.of(config);

        assertFalse(filter.isTarget("Target0", String.class));

        filter.addTransformed(String.class);

        assertFalse(filter.isTarget("Target0", String.class));
        assertFalse(filter.isTarget("Target1", String.class));
    }

    @Test
    public void beansNamePattern() {
        Properties properties = new Properties();
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "Target.*");
        ProfilerConfig config = new DefaultProfilerConfig(properties);
        TargetBeanFilter filter = TargetBeanFilter.of(config);

        assertTrue(filter.isTarget("Target0", String.class));

        filter.addTransformed(String.class);

        assertFalse(filter.isTarget("Target0", String.class));
        assertFalse(filter.isTarget("Target1", String.class));
    }

    @Test
    public void classNamePattern() {
        Properties properties = new Properties();
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "java.lang.String");
        ProfilerConfig config = new DefaultProfilerConfig(properties);
        TargetBeanFilter filter = TargetBeanFilter.of(config);

        assertTrue(filter.isTarget("Target0", String.class));

        filter.addTransformed(String.class);

        assertFalse(filter.isTarget("Target0", String.class));
        assertFalse(filter.isTarget("Target1", String.class));
    }

    @Test
    public void annotation() {
        Properties properties = new Properties();
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Controller");
        ProfilerConfig config = new DefaultProfilerConfig(properties);
        TargetBeanFilter filter = TargetBeanFilter.of(config);

        assertFalse(filter.isTarget("Target0", String.class));

        filter.addTransformed(String.class);

        assertFalse(filter.isTarget("Target0", String.class));
        assertFalse(filter.isTarget("Target1", String.class));
    }


    @Test
    public void target0() {
        Properties properties = new Properties();
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "Target.*");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "java.lang.String");

        ProfilerConfig config = new DefaultProfilerConfig(properties);
        TargetBeanFilter filter = TargetBeanFilter.of(config);
        assertTrue(filter.isTarget("Target0", String.class));

        filter.addTransformed(String.class);

        // after transformed
        assertFalse(filter.isTarget("Target0", String.class));
        assertFalse(filter.isTarget("Target1", String.class));
    }

    @Test
    public void target1() {
        Properties properties = new Properties();
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "Target.*");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "java.lang.String");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Controller");

        ProfilerConfig config = new DefaultProfilerConfig(properties);
        TargetBeanFilter filter = TargetBeanFilter.of(config);
        assertFalse(filter.isTarget("Target0", String.class));

        filter.addTransformed(String.class);

        // after transformed
        assertFalse(filter.isTarget("Target0", String.class));
        assertFalse(filter.isTarget("Target1", String.class));
    }

    @Test
    public void target2() {
        Properties properties = new Properties();
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "");

        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "Target.*");

        ProfilerConfig config = new DefaultProfilerConfig(properties);
        TargetBeanFilter filter = TargetBeanFilter.of(config);
        assertTrue(filter.isTarget("Target0", String.class));

        filter.addTransformed(String.class);

        // after transformed
        assertFalse(filter.isTarget("Target0", String.class));
        assertFalse(filter.isTarget("Target1", String.class));
    }

    @Test
    public void target3() {
        Properties properties = new Properties();
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "Target.*");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "java.lang.String");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 3 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Controller");

        ProfilerConfig config = new DefaultProfilerConfig(properties);
        TargetBeanFilter filter = TargetBeanFilter.of(config);
        assertTrue(filter.isTarget("Target0", String.class));

        filter.addTransformed(String.class);

        // after transformed
        assertFalse(filter.isTarget("Target0", String.class));
        assertFalse(filter.isTarget("Target1", String.class));
    }

    @Test
    public void target4() {
        Properties properties = new Properties();
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "Target.*");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "");

        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "Target.*");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "java.lang.String");

        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 3 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "Target.*");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 3 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Controller");

        ProfilerConfig config = new DefaultProfilerConfig(properties);
        TargetBeanFilter filter = TargetBeanFilter.of(config);
        assertTrue(filter.isTarget("Target0", String.class));
        assertFalse(filter.isTarget("Foo", String.class));

        filter.addTransformed(String.class);

        // after transformed
        assertFalse(filter.isTarget("Target0", String.class));
        assertFalse(filter.isTarget("Target1", String.class));
    }

    @Test
    public void target5() {
        Properties properties = new Properties();
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "Target1, Target2");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "");

        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "Target0");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Controller");

        ProfilerConfig config = new DefaultProfilerConfig(properties);
        TargetBeanFilter filter = TargetBeanFilter.of(config);
        assertFalse(filter.isTarget("Target0", String.class));
        assertTrue(filter.isTarget("Target1", String.class));
        assertTrue(filter.isTarget("Target2", String.class));

        filter.addTransformed(String.class);

        // after transformed
        assertFalse(filter.isTarget("Target0", String.class));
        assertFalse(filter.isTarget("Target1", String.class));
        assertFalse(filter.isTarget("Target2", String.class));
    }

    @Test
    public void target6() {
        Properties properties = new Properties();
        properties.put(SpringBeansConfig.SPRING_BEANS_NAME_PATTERN, "foo");

        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "Target1, Target2");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "");

        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "Target0");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Controller");

        ProfilerConfig config = new DefaultProfilerConfig(properties);
        TargetBeanFilter filter = TargetBeanFilter.of(config);
        assertTrue(filter.isTarget("foo", String.class));
        assertFalse(filter.isTarget("Target0", String.class));
        assertTrue(filter.isTarget("Target1", String.class));
        assertTrue(filter.isTarget("Target2", String.class));

        filter.addTransformed(String.class);

        // after transformed
        assertFalse(filter.isTarget("foo", String.class));
        assertFalse(filter.isTarget("Target0", String.class));
        assertFalse(filter.isTarget("Target1", String.class));
        assertFalse(filter.isTarget("Target2", String.class));
    }
}