plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    testCompileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    testCompileOnly(project(":pinpoint-commons"))
    compileOnly("org.apache.kafka:kafka-clients:0.11.0.1")
    testCompileOnly("org.apache.kafka:kafka-clients:0.11.0.1")
}

description = "pinpoint-kafka-plugin"
