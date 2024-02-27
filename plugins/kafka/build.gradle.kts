plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    testCompileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    testCompileOnly(project(":pinpoint-commons"))
    compileOnly("org.apache.kafka:kafka-clients:3.1.0")
    testCompileOnly("org.apache.kafka:kafka-clients:3.1.0")
}

description = "pinpoint-kafka-plugin"
