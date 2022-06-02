plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
    `java-library-distribution`
}

dependencies {
    api(project(":pinpoint-annotations"))
    api(project(":pinpoint-commons-buffer"))
    api(project(":pinpoint-commons-profiler"))
    api(project(":pinpoint-profiler"))
    api(project(":pinpoint-profiler-test"))
    api(project(":pinpoint-grpc"))
    api(project(":pinpoint-bootstrap-core"))
    api(platform(project(":pinpoint-profiler-optional")))
    api(platform(project(":pinpoint-plugins")))
    api(platform(project(":pinpoint-agent-plugins")))
    implementation(libs.log4j.api.jdk7)
    runtimeOnly(project(":pinpoint-bootstrap-java8"))
    runtimeOnly(project(":pinpoint-tools"))
    runtimeOnly(libs.slf4j.api)
    runtimeOnly(libs.log4j.core.jdk7)
    runtimeOnly(libs.log4j.slf4j.impl.jdk7)
}

description = "pinpoint-agent-distribution"

distributions {

}
