/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-bootstrap-core"))
    implementation(libs.log4j.api.jdk7)
    implementation(libs.junit)
}

description = "pinpoint-plugin-it-jdbc-test"
