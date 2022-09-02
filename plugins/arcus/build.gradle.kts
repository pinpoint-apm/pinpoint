plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly("com.navercorp.arcus:arcus-java-client:1.8.1")
    testCompileOnly("com.navercorp.arcus:arcus-java-client:1.8.1")
    testImplementation(libs.log4j.api.jdk7)
    testImplementation(libs.log4j.slf4j.impl.jdk7)
    testImplementation(libs.log4j.core.jdk7)
}

description = "pinpoint-arcus-plugin"
