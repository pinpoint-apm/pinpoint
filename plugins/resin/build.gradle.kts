/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("com.navercorp.pinpoint.java-conventions")
}

dependencies {
    implementation(project(":pinpoint-common-servlet"))
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("javax.servlet:javax.servlet-api:3.0.1")
    compileOnly("com.caucho:resin:3.0.9")
}

description = "pinpoint-resin-plugin"
