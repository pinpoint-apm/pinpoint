/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java-conventions")
}

dependencies {
    compileOnly(project(":pinpoint-commons"))
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("javax.servlet:javax.servlet-api:3.0.1")
}

description = "pinpoint-common-servlet"
