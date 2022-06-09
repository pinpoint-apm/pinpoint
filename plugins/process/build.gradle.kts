plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-bootstrap-core"))
}

description = "pinpoint-process-plugin"
