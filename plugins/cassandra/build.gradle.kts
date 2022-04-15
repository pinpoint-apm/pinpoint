/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.toolchain.java7")
}

dependencies {
    testImplementation("org.apache.logging.log4j:log4j-api:${Versions.log4jJDK7}")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:${Versions.log4jJDK7}")
    testImplementation("org.apache.logging.log4j:log4j-core:${Versions.log4jJDK7}")
    testImplementation(project(":pinpoint-test"))
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("com.datastax.cassandra:cassandra-driver-core:2.1.7.1")
}

description = "pinpoint-cassandra-driver-plugin"
