/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("com.navercorp.pinpoint.java-conventions")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("org.redisson:redisson:3.10.4")
}

description = "pinpoint-redis-redisson-plugin"
