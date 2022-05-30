/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java7")
}

dependencies {
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-commons-buffer"))
    implementation("org.apache.thrift:libthrift")
    implementation(libs.log4j.slf4j.impl.jdk7)
    implementation(libs.log4j.core.jdk7)
    implementation(libs.log4j.jcl.jdk7)
    runtimeOnly(libs.slf4j.api)
    testImplementation(libs.commons.lang)
}

description = "pinpoint-thrift"
