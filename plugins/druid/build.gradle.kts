/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("com.navercorp.pinpoint.java-conventions")
}

dependencies {
    providedCompile(project(":pinpoint-bootstrap-core"))
    compileOnly("com.alibaba:druid:1.1.10")
}

description = "pinpoint-druid-plugin"
