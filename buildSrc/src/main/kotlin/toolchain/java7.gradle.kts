package com.navercorp.pinpoint.gradle.plugins.toolchain

import gradle.kotlin.dsl.accessors._f4124090576aed771f557cac1ca7fd48.java

plugins {
    id("com.navercorp.pinpoint.gradle.plugins.java-library")
    id("com.navercorp.pinpoint.gradle.plugins.dependency.java7")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(7))
    }
}
