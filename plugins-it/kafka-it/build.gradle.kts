plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation(project(":pinpoint-kafka-plugin"))
    testImplementation("org.apache.kafka:kafka-clients:0.11.0.1")
    testImplementation(libs.log4j)
    testImplementation("org.apache.kafka:kafka_2.12:2.6.0")
    testImplementation(libs.commons.io)
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
    testImplementation(libs.log4j.api)
}

description = "pinpoint-kafka-plugin-it"
