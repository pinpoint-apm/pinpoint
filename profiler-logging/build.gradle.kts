plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-annotations"))
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-bootstrap-core"))
    api(project(":pinpoint-bootstrap"))
    implementation(libs.log4j.slf4j.impl.jdk7)
    implementation(libs.log4j.core.jdk7)
    implementation(libs.log4j.api.jdk7)
}

description = "pinpoint-profiler-logging"
