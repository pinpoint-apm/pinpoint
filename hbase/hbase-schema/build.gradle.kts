/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java11-toolchain")
    id("pinpoint.hbase-bom")
}

dependencies {
    api(project(":pinpoint-hbase-schema-definition"))
    api(project(":pinpoint-commons-hbase"))
    api(project(":pinpoint-commons-buffer"))
    implementation("org.springframework:spring-core")
    implementation("org.apache.logging.log4j:log4j-api:${Versions.log4jJDK8}")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:${Versions.jaxbImpl}")
    runtimeOnly("com.sun.xml.bind:jaxb-impl:${Versions.jaxbImpl}")
}

description = "pinpoint-hbase-schema"
