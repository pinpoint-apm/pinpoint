/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java7-toolchain")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("org.apache.cxf:cxf-rt-frontend-jaxrs:3.0.16")
    testImplementation("org.apache.cxf:cxf-rt-frontend-jaxws:3.0.16")
    testImplementation("org.apache.cxf:cxf-rt-transports-http:3.0.16")
    testImplementation("org.apache.cxf:cxf-rt-rs-client:3.0.16")
}

description = "pinpoint-cxf-plugin-it"
