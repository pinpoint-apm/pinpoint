plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnlyApi("com.typesafe.akka:akka-http-core_2.12:10.1.0-RC1")
    compileOnlyApi("com.typesafe.akka:akka-http_2.12:10.1.0-RC1")
    compileOnlyApi(project(":pinpoint-bootstrap-core"))
}

description = "pinpoint-akka-http-plugin"
