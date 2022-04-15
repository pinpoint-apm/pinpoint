package com.navercorp.pinpoint.gradle.plugins.dependency

plugins {
    id("io.spring.dependency-management")
}

dependencyManagement {
    dependencies {
        dependency("org.springframework:spring-core:${Versions.spring4}") {
            exclude("commons-logging:commons-logging")
        }
        dependency("org.springframework:spring-beans:${Versions.spring4}")
        dependency("org.springframework:spring-context:${Versions.spring4}")
        dependency("org.springframework:spring-orm:${Versions.spring4}")
        dependency("org.springframework:spring-web:${Versions.spring4}")
        dependency("org.springframework:spring-webmvc:${Versions.spring4}")
        dependency("org.springframework:spring-websocket:${Versions.spring4}")
        dependency("org.springframework:spring-jdbc:${Versions.spring4}")
        dependency("org.springframework:spring-tx:${Versions.spring4}")
        dependency("org.springframework:spring-context-support:${Versions.spring4}")
    }
}