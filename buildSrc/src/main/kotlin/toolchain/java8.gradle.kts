package com.navercorp.pinpoint.gradle.plugins.toolchain

plugins {
    id("com.navercorp.pinpoint.gradle.plugins.java-library")
    id("com.navercorp.pinpoint.gradle.plugins.dependency.java")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}
