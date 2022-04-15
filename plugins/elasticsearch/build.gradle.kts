/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.toolchain.java8")
}

dependencies {
    testImplementation("org.slf4j:slf4j-api:${Versions.slf4j}")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:${Versions.log4jJDK7}")
    testImplementation("org.apache.logging.log4j:log4j-core:${Versions.log4jJDK7}")
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("org.elasticsearch.client:elasticsearch-rest-high-level-client:7.3.0")
}

description = "pinpoint-elasticsearch-plugin"
