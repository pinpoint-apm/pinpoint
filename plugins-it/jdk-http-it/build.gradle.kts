plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation(libs.jackson.core.asl)
    testImplementation(libs.jackson.mapper.asl)
    testImplementation(libs.jackson.core)
    testImplementation(libs.jackson.annotations)
    testImplementation(libs.jackson.databind)
    testImplementation(project(":pinpoint-bootstrap-core"))
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
    testImplementation(libs.nanohttpd)
}

description = "pinpoint-jdk-http-plugin-it"
