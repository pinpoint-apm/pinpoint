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
    testImplementation("com.google.http-client:google-http-client:1.20.0")
}

description = "pinpoint-google-httpclient-plugin-it"
