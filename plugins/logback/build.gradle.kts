plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(libs.slf4j.api)
    compileOnly("ch.qos.logback:logback-core:1.2.5")
}

description = "pinpoint-logback-plugin"
