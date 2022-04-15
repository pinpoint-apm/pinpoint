package com.navercorp.pinpoint.gradle.plugins.bom

plugins {
    `java-library`
}

dependencies {
    api(project(":pinpoint-profiler-optional-jdk7"))
    api(project(":pinpoint-profiler-optional-jdk8"))
    api(project(":pinpoint-profiler-optional-jdk9"))
}
