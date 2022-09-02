plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(project(":pinpoint-bootstrap-core"))
    implementation(project(":pinpoint-thrift"))
    implementation(project(":pinpoint-commons"))
    implementation(libs.libthrift.v012)
    implementation(libs.log4j.api.jdk7)
    runtimeOnly(libs.slf4j.api)
    runtimeOnly(libs.log4j.slf4j.impl.jdk7)
    runtimeOnly(libs.log4j.core.jdk7)
}

description = "pinpoint-tools"
