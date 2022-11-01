plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(project(":pinpoint-commons"))
    implementation(project(":pinpoint-profiler"))
    implementation(project(":pinpoint-thrift"))
    implementation(project(":pinpoint-bootstrap-core"))
    implementation(project(":pinpoint-bootstrap"))
    implementation(project(":pinpoint-rpc"))
    implementation(project(":pinpoint-plugins-loader"))
    implementation(project(":pinpoint-profiler-logging"))
    implementation(libs.asm.core)
    implementation(libs.asm.commons)
    implementation(libs.asm.util)
    implementation(libs.asm.tree)
    implementation(libs.libthrift)
    implementation(libs.guice)
    implementation(libs.log4j.core)
    implementation(libs.junit)
    runtimeOnly(libs.slf4j.api)
    runtimeOnly(libs.log4j.jcl)
    runtimeOnly(libs.log4j.slf4j.impl)
    runtimeOnly(libs.log4j.core)
    testImplementation(libs.commons.lang3)
}

description = "pinpoint-profiler-test"
