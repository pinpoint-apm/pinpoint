/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    api(project(":pinpoint-plugin-it-jdbc-test"))
    testImplementation("net.sourceforge.jtds:jtds:1.2.8")
    testImplementation(project(":pinpoint-jtds-plugin"))
    testImplementation("org.testcontainers:mssqlserver:1.16.2")
    testImplementation(project(":pinpoint-profiler-test"))
}

description = "pinpoint-jtds-plugin-it"
