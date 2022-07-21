plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
    `java-library-distribution`
}

dependencies {
    implementation(project(":pinpoint-annotations"))
    implementation(project(":pinpoint-commons-buffer"))
    implementation(project(":pinpoint-commons-profiler"))
    implementation(project(":pinpoint-profiler"))
    implementation(project(":pinpoint-profiler-test"))
    implementation(project(":pinpoint-grpc"))
    implementation(libs.log4j.api.jdk7)
    runtimeOnly(platform(project(":pinpoint-profiler-optional")))
    runtimeOnly(platform(project(":pinpoint-plugins")))
    runtimeOnly(platform(project(":pinpoint-agent-plugins")))
    runtimeOnly(project(":pinpoint-bootstrap-core"))
    runtimeOnly(project(":pinpoint-bootstrap-java8"))
    runtimeOnly(project(":pinpoint-tools"))
    runtimeOnly(libs.slf4j.api)
    runtimeOnly(libs.log4j.core.jdk7)
    runtimeOnly(libs.log4j.slf4j.impl.jdk7)
    testImplementation(project(":pinpoint-commons"))
    testImplementation(project(":pinpoint-plugins-loader"))
}

description = "pinpoint-agent-distribution"

distributions {

}
