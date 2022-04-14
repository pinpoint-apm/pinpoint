/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java7-toolchain")
}

dependencies {
    testImplementation("org.apache.logging.log4j:log4j-api:${Versions.log4jJDK7}")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:${Versions.log4jJDK7}")
    testImplementation("org.apache.logging.log4j:log4j-core:${Versions.log4jJDK7}")
    testImplementation("com.fasterxml.jackson.core:jackson-databind")
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("org.mongodb:mongodb-driver:3.9.0")
}

description = "pinpoint-mongodb-driver-plugin"
