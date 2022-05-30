/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java11")
    id("com.navercorp.pinpoint.gradle.plugins.bom.grpc")
}

dependencies {
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-commons-profiler"))
    api(project(":pinpoint-commons-hbase"))
    api(project(":pinpoint-plugins-loader"))
    implementation(libs.commons.collections4)
    implementation(libs.spring.core) {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation(libs.spring.context)
    implementation("org.springframework.boot:spring-boot:${Versions.springBoot}")
    implementation(libs.commons.lang3)
    implementation("org.apache.thrift:libthrift")
    implementation(libs.log4j.api.jdk7)
    runtimeOnly(libs.slf4j.api)
    runtimeOnly(libs.log4j.slf4j.impl)
    runtimeOnly(libs.log4j.core)
    runtimeOnly(libs.log4j.jcl)
    testImplementation("org.awaitility:awaitility")
    testImplementation(libs.spring.test)
    compileOnlyApi(project(":pinpoint-thrift"))
    compileOnlyApi(project(":pinpoint-grpc"))

    implementation(libs.hbase.shaded.client) {
        exclude("org.slf4j:slf4j-log4j12")
        exclude("commons-logging:commons-logging")
    }
    implementation(libs.hbasewd) {
        exclude("log4j:log4j")
    }
}

description = "pinpoint-commons-server"
