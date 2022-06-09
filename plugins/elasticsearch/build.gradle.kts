plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    testImplementation(libs.slf4j.api)
    testImplementation(libs.log4j.slf4j.impl.jdk7)
    testImplementation(libs.log4j.core.jdk7)
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("org.elasticsearch.client:elasticsearch-rest-high-level-client:7.3.0")
}

description = "pinpoint-elasticsearch-plugin"
