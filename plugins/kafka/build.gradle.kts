plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("org.apache.kafka:kafka-clients:0.11.0.1")
}

description = "pinpoint-kafka-plugin"
