/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java8-toolchain")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("com.ning:async-http-client:1.8.3")
    compileOnly("org.asynchttpclient:async-http-client:2.0.32")
}

description = "pinpoint-ning-asynchttpclient-plugin"

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "1.7"
    targetCompatibility = "1.7"
}