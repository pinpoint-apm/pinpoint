/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java7")
}

dependencies {
    api(project(":pinpoint-bootstrap"))
    implementation("org.apache.maven.resolver:maven-resolver-api:1.6.1")
    implementation("org.apache.maven.resolver:maven-resolver-spi:1.6.1")
    implementation("org.apache.maven.resolver:maven-resolver-util:1.6.1")
    implementation("org.apache.maven.resolver:maven-resolver-impl:1.6.1")
    implementation("org.apache.maven.resolver:maven-resolver-connector-basic:1.6.1")
    implementation("org.apache.maven.resolver:maven-resolver-transport-classpath:1.6.1")
    implementation("org.apache.maven.resolver:maven-resolver-transport-file:1.6.1")
    implementation("org.apache.maven.resolver:maven-resolver-transport-http:1.6.1")
    implementation("org.apache.maven.resolver:maven-resolver-transport-wagon:1.6.1")
    implementation("org.apache.maven:maven-resolver-provider:3.6.3")
    implementation("org.apache.commons:commons-lang3")
    implementation(libs.log4j.api.jdk7)
    implementation("org.tinylog:tinylog-api:${Versions.tinylog}")
    implementation("org.tinylog:tinylog-impl:${Versions.tinylog}") {
        exclude(group = "org.tinylog", module = "tinylog-api")
    }
    implementation("org.tinylog:slf4j-tinylog:${Versions.tinylog}") {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "org.tinylog", module = "tinylog-api")
    }
    implementation("junit:junit:${Versions.junit}")
    runtimeOnly(libs.slf4j.api)
    testImplementation(libs.log4j.slf4j.impl.jdk7) {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
        exclude(group = "org.apache.logging.log4j", module = "log4j-core")
    }
    testImplementation(libs.log4j.core.jdk7) {
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    }
    testImplementation(libs.log4j.jcl.jdk7) {
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    }
}

description = "pinpoint-test"

tasks {
    processResources {
        expand("version" to version)
    }
}