plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-annotations"))
    compileOnly("io.vertx:vertx-core:4.2.2")
}

description = "pinpoint-vertx-plugin"