plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    testImplementation(libs.log4j.api.jdk7)
    compileOnly(project(":pinpoint-bootstrap-core"))
}

description = "pinpoint-bootstrap-java8"
