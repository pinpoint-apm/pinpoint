/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java-conventions")
}

dependencies {
    implementation(project(":pinpoint-plugin-it-utils"))
    testImplementation("com.squareup.okhttp:okhttp:2.5.0")
    testImplementation("com.squareup.okhttp3:okhttp:3.8.1")
    testImplementation(project(":pinpoint-okhttp-plugin"))
}

description = "pinpoint-okhttp-plugin-it"
