/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java8-toolchain")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-logging:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-actuator:${Versions.springBoot}")
    implementation("org.springdoc:springdoc-openapi-ui:1.4.4")
    implementation("commons-io:commons-io")
    compileOnly("org.springframework.boot:spring-boot-starter-tomcat:${Versions.springBoot}")
}

description = "pinpoint-jdk-http-plugin-testweb"
