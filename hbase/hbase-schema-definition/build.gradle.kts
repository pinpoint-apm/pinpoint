/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java-conventions")
}

dependencies {
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:2.3.3")
    runtimeOnly("com.sun.xml.bind:jaxb-impl:2.3.3")
}

description = "pinpoint-hbase-schema-definition"
