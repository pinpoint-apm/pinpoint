/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java-conventions")
}

dependencies {
    implementation(project(":pinpoint-agent-proxy-common"))
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-profiler"))
}

description = "pinpoint-agent-proxy-nginx-plugin"
