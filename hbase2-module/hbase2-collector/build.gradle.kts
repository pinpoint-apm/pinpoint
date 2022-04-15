/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.toolchain.java11")
}

dependencies {
    api(project(":pinpoint-collector"))
    implementation("org.apache.hbase:hbase-client:2.4.2")
    implementation("org.springframework.boot:spring-boot-autoconfigure:${Versions.springBoot}")
}

description = "pinpoint-hbase2-collector"
