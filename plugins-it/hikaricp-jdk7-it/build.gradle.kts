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
    testImplementation("com.zaxxer:HikariCP-java6:2.3.13")
}

description = "pinpoint-hikaricp-plugin-jdk7-it"