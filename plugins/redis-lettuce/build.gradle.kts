/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("io.lettuce:lettuce-core:5.1.2.RELEASE")
}

description = "pinpoint-redis-lettuce-plugin"
