plugins {
    id("com.navercorp.pinpoint.java8-library")
    `java-library-distribution`
}

dependencies {
    implementation(project(":pinpoint-annotations"))
    implementation(project(":pinpoint-commons-buffer"))
    implementation(project(":pinpoint-commons-profiler"))
    implementation(project(":pinpoint-profiler"))
    implementation(project(":pinpoint-profiler-test"))
    implementation(project(":pinpoint-grpc"))
    implementation(libs.log4j.api)
    runtimeOnly(platform(project(":pinpoint-profiler-optional")))
    runtimeOnly(platform(project(":pinpoint-plugins")))
    runtimeOnly(platform(project(":pinpoint-agent-plugins")))
    runtimeOnly(project(":pinpoint-bootstrap-core"))
    runtimeOnly(project(":pinpoint-bootstrap-java8"))
    runtimeOnly(project(":pinpoint-tools"))
    runtimeOnly(libs.slf4j.api)
    runtimeOnly(libs.log4j.core)
    runtimeOnly(libs.log4j.slf4j.impl)
    testImplementation(project(":pinpoint-commons"))
    testImplementation(project(":pinpoint-plugins-loader"))
}

description = "pinpoint-agent-distribution"

distributions {

}
