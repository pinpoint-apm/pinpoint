plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    implementation(project(":pinpoint-agent-proxy-common"))
    compileOnly(project(":pinpoint-profiler"))
    testCompileOnly(project(":pinpoint-profiler"))
    compileOnly(project(":pinpoint-bootstrap-core"))
}

description = "pinpoint-agent-proxy-apache-plugin"
