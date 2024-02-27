plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    testCompileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly(project(":pinpoint-annotations"))

    val vertx = "4.2.2"
    compileOnly("io.vertx:vertx-core:${vertx}")
    compileOnly("io.vertx:vertx-web:${vertx}")
}

description = "pinpoint-vertx-plugin"