plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-agent-proxy-common"))
    api(project(":pinpoint-profiler"))
    compileOnly(project(":pinpoint-bootstrap-core"))
}

description = "pinpoint-agent-proxy-app-plugin"
