package com.navercorp.pinpoint.plugin.spring.boot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;

class SpringBootDetectorTest {

    @Test
    void checkAnnotation() {
        SpringBootDetector detector = new SpringBootDetector(null);

        List<String> annotations = Collections.singletonList(
                TestAnnotation.class.getName()
        );

        String mainClass = TestMainClass.class.getName();
        ClassLoader classLoader = TestMainClass.class.getClassLoader();

        boolean isExist = detector.checkAnnotation(mainClass, classLoader, annotations);
        Assertions.assertTrue(isExist);

        String detectorTest = SpringBootDetectorTest.class.getName();
        boolean test = detector.checkAnnotation(detectorTest, classLoader, annotations);
        Assertions.assertFalse(test);
    }

    @TestAnnotation
    public static class TestMainClass {
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestAnnotation {
    }
}