/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java8-conventions")
    id("pinpoint.profiler-optional-conventions")
    id("pinpoint.plugins-assembly-conventions")
    id("pinpoint.agent-plugins-conventions")
    id("pinpoint.bootstraps-conventions")
}

dependencies {
    api(project(":pinpoint-annotations"))
    api(project(":pinpoint-commons-buffer"))
    api(project(":pinpoint-commons-profiler"))
    api(project(":pinpoint-profiler"))
    api(project(":pinpoint-profiler-test"))
    api(project(":pinpoint-grpc"))
    implementation("org.apache.logging.log4j:log4j-api:2.12.4")
    runtimeOnly(project(":pinpoint-tools"))
    runtimeOnly("org.slf4j:slf4j-api:1.7.30")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.12.4")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.12.4")
}

description = "pinpoint-agent-distribution"
