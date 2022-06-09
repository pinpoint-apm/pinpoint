plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("com.navercorp.arcus:arcus-java-client:1.8.1")
    testImplementation(libs.log4j.api.jdk7)
    testImplementation(libs.log4j.slf4j.impl.jdk7)
    testImplementation(libs.log4j.core.jdk7)
    testImplementation("com.navercorp.arcus:arcus-java-client:1.8.1")
}

description = "pinpoint-arcus-plugin"
