/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java11-toolchain")
}

dependencies {
    api(project(":pinpoint-flink"))
    implementation("org.apache.hbase:hbase-client:2.4.2")
}

description = "pinpoint-hbase2-flink"
