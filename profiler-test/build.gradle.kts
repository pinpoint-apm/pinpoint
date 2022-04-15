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
    api(project(":pinpoint-profiler"))
    api(project(":pinpoint-thrift"))
    api(project(":pinpoint-bootstrap-core"))
    api(project(":pinpoint-bootstrap"))
    api(project(":pinpoint-rpc"))
    implementation("org.ow2.asm:asm")
    implementation("org.ow2.asm:asm-commons")
    implementation("org.ow2.asm:asm-util")
    implementation("org.ow2.asm:asm-tree")
    implementation("org.apache.thrift:libthrift")
    implementation("com.google.inject:guice")
    implementation("org.apache.logging.log4j:log4j-core:${Versions.log4jJDK7}")
    implementation("junit:junit:${Versions.junit}")
    runtimeOnly("org.slf4j:slf4j-api:${Versions.slf4j}")
    runtimeOnly("org.apache.logging.log4j:log4j-jcl:${Versions.log4jJDK7}")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:${Versions.log4jJDK7}")
    runtimeOnly("org.apache.logging.log4j:log4j-core:${Versions.log4jJDK7}")
    testImplementation("org.apache.commons:commons-lang3")
}

description = "pinpoint-profiler-test"
