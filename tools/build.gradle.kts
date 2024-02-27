plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(project(":pinpoint-bootstrap-core"))
    implementation(project(":pinpoint-thrift"))
    implementation(project(":pinpoint-commons"))
    implementation(libs.libthrift)
    implementation(libs.log4j.api)
    runtimeOnly(libs.slf4j.api)
    runtimeOnly(libs.log4j.slf4j.impl)
    runtimeOnly(libs.log4j.core)
}

description = "pinpoint-tools"
