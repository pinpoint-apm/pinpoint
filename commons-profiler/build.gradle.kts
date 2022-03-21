/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java7-conventions")
}

dependencies {
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-commons-buffer"))
    testImplementation("org.apache.logging.log4j:log4j-api:2.12.4")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.12.4")
    testImplementation("org.apache.logging.log4j:log4j-core:2.12.4")
    testImplementation("org.apache.logging.log4j:log4j-jcl:2.12.4")
    testImplementation("org.apache.commons:commons-lang3:3.8.1")
}

description = "pinpoint-commons-profiler"
