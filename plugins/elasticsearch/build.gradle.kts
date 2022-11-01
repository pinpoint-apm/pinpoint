plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    testImplementation(libs.slf4j.api)
    testImplementation(libs.log4j.slf4j.impl)
    testImplementation(libs.log4j.core)
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly("org.elasticsearch.client:elasticsearch-rest-high-level-client:7.3.0")
}

description = "pinpoint-elasticsearch-plugin"
