plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly(libs.log4j)
}

description = "pinpoint-log4j-plugin"
