/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java-conventions")
}

dependencies {
    implementation(project(":pinpoint-plugin-it-utils"))
    testImplementation("org.apache.activemq:activemq-all:5.13.2")
    testImplementation(project(":pinpoint-profiler-test"))
}

description = "pinpoint-activemq-plugin-it"
