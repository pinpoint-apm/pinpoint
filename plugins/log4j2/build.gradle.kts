plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(libs.log4j.core.jdk7)
    compileOnly(libs.log4j.api.jdk7)
}

description = "pinpoint-log4j2-plugin"
