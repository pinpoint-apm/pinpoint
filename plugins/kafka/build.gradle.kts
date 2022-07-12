plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnlyApi(project(":pinpoint-bootstrap-core"))
    compileOnlyApi("org.apache.kafka:kafka-clients:0.11.0.1")
}

description = "pinpoint-kafka-plugin"
