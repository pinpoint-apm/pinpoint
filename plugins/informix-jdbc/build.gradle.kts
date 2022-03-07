/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java-conventions")
}

dependencies {
    testImplementation("org.apache.logging.log4j:log4j-api:2.12.4")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.12.4")
    testImplementation("org.apache.logging.log4j:log4j-core:2.12.4")
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("com.ibm.informix:jdbc:4.10.10.0")
}

description = "pinpoint-informix-jdbc-driver-plugin"
