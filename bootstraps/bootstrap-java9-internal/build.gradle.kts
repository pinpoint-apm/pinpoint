/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java11-conventions")
}

dependencies {
    api(project(":pinpoint-bootstrap-core"))
    api(project(":pinpoint-commons"))
    testImplementation("org.apache.logging.log4j:log4j-api:2.12.4")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.12.4")
    testImplementation("org.apache.logging.log4j:log4j-core:2.12.4")
    testImplementation("org.apache.logging.log4j:log4j-jcl:2.12.4")
    testImplementation("mysql:mysql-connector-java:8.0.27")
    testImplementation("commons-io:commons-io:2.6")
}

description = "pinpoint-bootstrap-java9-internal"

tasks.compileJava {
    options.release.set(9)
}