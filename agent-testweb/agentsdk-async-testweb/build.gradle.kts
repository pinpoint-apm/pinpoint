/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java-conventions")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:2.5.7")
    implementation("org.springframework.boot:spring-boot-starter-logging:2.5.7")
    implementation("org.springframework.boot:spring-boot-starter-actuator:2.5.7")
    implementation("org.springdoc:springdoc-openapi-ui:1.4.4")
    api(project(":pinpoint-agent-sdk"))
    compileOnly("org.springframework.boot:spring-boot-starter-tomcat:2.5.7")
}

description = "pinpoint-agentsdk-async-testweb"
