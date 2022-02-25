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
    compileOnly("org.apache.commons:commons-dbcp2:2.1.1")
}

description = "pinpoint-commons-dbcp2-plugin"
