/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java-conventions")
}

dependencies {
    implementation(project(":pinpoint-common-servlet"))
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("javax.servlet:javax.servlet-api:3.0.1")
    compileOnly("io.undertow:undertow-core:2.0.1.Final")
}

description = "pinpoint-undertow-servlet-plugin"
