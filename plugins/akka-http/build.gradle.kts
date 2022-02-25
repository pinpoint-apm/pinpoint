/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("com.navercorp.pinpoint.java-conventions")
}

dependencies {
    compileOnly("com.typesafe.akka:akka-http-core_2.12:10.1.0-RC1")
    compileOnly("com.typesafe.akka:akka-http_2.12:10.1.0-RC1")
    providedCompile(project(":pinpoint-bootstrap-core"))
}

description = "pinpoint-akka-http-plugin"
