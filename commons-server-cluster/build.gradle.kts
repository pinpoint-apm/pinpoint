/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java11")
    id("com.navercorp.pinpoint.gradle.plugins.bom.curator")
}

dependencies {
    implementation("org.apache.zookeeper:zookeeper")
    implementation(libs.spring.context)
    implementation("org.springframework.boot:spring-boot:${Versions.springBoot}")
    implementation(libs.log4j.api.jdk7)
    runtimeOnly(libs.slf4j.api)
    runtimeOnly(libs.log4j.slf4j.impl)
    runtimeOnly(libs.log4j.core)
    runtimeOnly(libs.log4j.jcl)
    testImplementation(libs.spring.test)
    testImplementation("org.apache.curator:curator-test")
    testImplementation(project(":pinpoint-testcase"))
    testImplementation("org.awaitility:awaitility")
}

description = "pinpoint-commons-server-cluster"
