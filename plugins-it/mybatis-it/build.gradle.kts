/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java-conventions")
}

dependencies {
    implementation("org.springframework:spring-beans:5.3.13")
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("org.mybatis:mybatis:3.5.7")
    testImplementation("org.mybatis:mybatis-spring:2.0.6")
}

description = "pinpoint-mybatis-plugin-it"
