/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("com.navercorp.pinpoint.java-conventions")
}

dependencies {
    implementation(project(":pinpoint-commons"))
    implementation("org.nanohttpd:nanohttpd:2.3.1")
}

description = "pinpoint-plugin-it-utils"
