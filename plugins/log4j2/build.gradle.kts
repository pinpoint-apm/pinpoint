/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("com.navercorp.pinpoint.java-conventions")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("org.apache.logging.log4j:log4j-core:2.12.4")
    compileOnly("org.apache.logging.log4j:log4j-api:2.12.4")
}

description = "pinpoint-log4j2-plugin"
