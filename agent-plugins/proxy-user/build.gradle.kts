plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-agent-proxy-common"))
    api(project(":pinpoint-profiler"))
    compileOnly(project(":pinpoint-bootstrap-core"))
    testImplementation(libs.log4j.api)
}

description = "pinpoint-agent-proxy-user-plugin"
