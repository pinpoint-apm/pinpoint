/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-commons"))
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(libs.javax.servlet.api.v3)
}

description = "pinpoint-common-servlet"
