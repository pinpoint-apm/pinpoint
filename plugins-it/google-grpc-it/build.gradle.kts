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
    testImplementation("io.grpc:grpc-protobuf:1.14.0")
    testImplementation("io.grpc:grpc-stub:1.14.0")
    testImplementation("io.grpc:grpc-netty:1.14.0")
    testImplementation(libs.netty.all)
}

description = "pinpoint-grpc-plugin-it"
