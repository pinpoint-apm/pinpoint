package com.navercorp.pinpoint.plugin.spring.beans;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import org.junit.Test;

import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

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
        ProfilerConfig config = new DefaultProfilerConfig(properties);

        SpringBeansConfig springBeansConfig = new SpringBeansConfig(config);
        assertEquals(3, springBeansConfig.getTargets().size());
    }


    @Test
    public void backwardCompatibility() {
        Properties properties = new Properties();
        properties.put(SpringBeansConfig.SPRING_BEANS_NAME_PATTERN, "");
        properties.put(SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN, "");
        properties.put(SpringBeansConfig.SPRING_BEANS_ANNOTATION, "org.springframework.stereotype.Controller, org.springframework.stereotype.Servicem, org.springframework.stereotype.Repository");

        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "Target.*");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Controller");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "java.lang.String");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Service");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 3 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Repository");
        ProfilerConfig config = new DefaultProfilerConfig(properties);

        SpringBeansConfig springBeansConfig = new SpringBeansConfig(config);

        // backward compatiblity.
        assertEquals(4, springBeansConfig.getTargets().size());
    }

    @Test
    public void max() {
        Properties properties = new Properties();
        properties.put(SpringBeansConfig.SPRING_BEANS_MAX, "10");

        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "Target.*");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Controller");

        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 9 + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "java.lang.String");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 9 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Service");

        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 100 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Repository");
        ProfilerConfig config = new DefaultProfilerConfig(properties);

        SpringBeansConfig springBeansConfig = new SpringBeansConfig(config);

        // max.
        assertEquals(2, springBeansConfig.getTargets().size());
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

        ProfilerConfig config = new DefaultProfilerConfig(properties);

        SpringBeansConfig springBeansConfig = new SpringBeansConfig(config);

        // excluded empty.
        assertEquals(2, springBeansConfig.getTargets().size());
    }

    @Test
    public void pattern() {
        Properties properties = new Properties();
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "Target.*");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 1 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Controller");
        // empty
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX, "");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 2 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "");

        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 6 + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "java.lang.String");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + 6 + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Service");

        // old
        properties.put(SpringBeansConfig.SPRING_BEANS_NAME_PATTERN, "com.navercorp.*");

        // wrong number
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + "A" + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "java.lang.String");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + "A" + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Service");

        // not found number
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX, "java.lang.String");
        properties.put(SpringBeansConfig.SPRING_BEANS_PREFIX + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX, "org.springframework.stereotype.Service");

        ProfilerConfig config = new DefaultProfilerConfig(properties);
        SpringBeansConfig springBeansConfig = new SpringBeansConfig(config);

        for(SpringBeansTarget target : springBeansConfig.getTargets()) {
            System.out.println(target);
        }
    }
}