/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java7")
}

dependencies {
    implementation(libs.spring4.beans)
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("org.mybatis:mybatis")
    testImplementation("org.mybatis:mybatis-spring")
}

description = "pinpoint-mybatis-plugin-it"
