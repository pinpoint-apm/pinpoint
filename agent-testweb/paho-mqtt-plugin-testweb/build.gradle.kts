/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java8-conventions")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-logging:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-actuator:${Versions.springBoot}")
    implementation("org.springdoc:springdoc-openapi-ui:1.4.4")
    compileOnly("org.springframework.boot:spring-boot-starter-tomcat:${Versions.springBoot}")
    compileOnly("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    compileOnly("org.eclipse.paho:org.eclipse.paho.mqttv5.client:1.2.5")
}

description = "pinpoint-paho-mqtt-plugin-testweb"
