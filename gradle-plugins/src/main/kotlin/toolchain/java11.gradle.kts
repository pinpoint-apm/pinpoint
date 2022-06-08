package com.navercorp.pinpoint.gradle.plugins.toolchain

plugins {
    id("com.navercorp.pinpoint.gradle.plugins.java-library")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}
