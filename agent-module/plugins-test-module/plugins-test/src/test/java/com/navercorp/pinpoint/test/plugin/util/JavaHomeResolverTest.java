package com.navercorp.pinpoint.test.plugin.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


class JavaHomeResolverTest {

    @Test
    void javaExecutable() {

        JavaHomeResolver.JavaHome java8 = new JavaHomeResolver.JavaHome("/java8", 8);
        JavaHomeResolver.JavaHome java11 = new JavaHomeResolver.JavaHome("/java11", 11);

        Map<String, String> javaHome = new HashMap<>();
        javaHome.put("JAVA_8_HOME", java8.home());
        javaHome.put("JAVA_11_HOME", java11.home());
        javaHome.put("JAVA_HOME", java11.home());

        JavaHomeResolver executable = new JavaHomeResolver(javaHome);
        List<JavaHomeResolver.JavaHome> javaHomes = executable.getJavaHomes();

        Assertions.assertTrue(javaHomes.contains(java8));
        Assertions.assertTrue(javaHomes.contains(java11));

        Optional<JavaHomeResolver.JavaHome> excludeHome = javaHomes.stream()
                .filter(java -> java.home().equals("JAVA_HOME")).findFirst();
        Assertions.assertFalse(excludeHome.isPresent());

    }

    @Test
    void buildJavaExecutable() {

        JavaHomeResolver.JavaHome java8 = new JavaHomeResolver.JavaHome("/java8", 8);

        Map<String, String> javaHome = new HashMap<>();
        javaHome.put("JAVA_8_HOME", java8.home());

        JavaHomeResolver executable = new JavaHomeResolver(javaHome);
        String path = executable.buildJavaExecutable(8);
        Assertions.assertNotNull(path);

    }
}