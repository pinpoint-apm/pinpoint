/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java7")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation(libs.spring4.test)
    testImplementation(libs.spring4.context)
    testImplementation("org.springframework:spring-webmvc:${Versions.spring4}")
    testImplementation("javax.servlet:javax.servlet-api")
}

description = "pinpoint-spring-plugin-it"
