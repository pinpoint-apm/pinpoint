plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    testImplementation(libs.log4j.api.jdk7)
    testImplementation(libs.log4j.slf4j.impl.jdk7)
    testImplementation(libs.log4j.core.jdk7)
    testImplementation(libs.jackson.databind)
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("org.mongodb:mongodb-driver:3.9.0")
}

description = "pinpoint-mongodb-driver-plugin"
