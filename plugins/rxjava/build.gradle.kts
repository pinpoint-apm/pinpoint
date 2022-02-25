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
    compileOnly("io.reactivex:rxjava:1.2.0")
}

description = "pinpoint-rxjava-plugin"
