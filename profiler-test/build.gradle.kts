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
    implementation(libs.libthrift.v012)
    implementation(libs.guice)
    implementation(libs.log4j.core.jdk7)
    implementation(libs.junit)
    runtimeOnly(libs.slf4j.api)
    runtimeOnly(libs.log4j.jcl.jdk7)
    runtimeOnly(libs.log4j.slf4j.impl.jdk7)
    runtimeOnly(libs.log4j.core.jdk7)
    testImplementation(libs.commons.lang3)
}

description = "pinpoint-profiler-test"
