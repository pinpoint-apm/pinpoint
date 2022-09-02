plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    testCompileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly(project(":pinpoint-annotations"))
    compileOnly("io.vertx:vertx-core:4.2.2")
}

description = "pinpoint-vertx-plugin"