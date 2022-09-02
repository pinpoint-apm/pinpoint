plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    testImplementation(libs.log4j.api.jdk7)
    compileOnly(project(":pinpoint-bootstrap-core"))
}

description = "pinpoint-bootstrap-java8"
