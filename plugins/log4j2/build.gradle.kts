plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    testCompileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    testCompileOnly(project(":pinpoint-commons"))
    compileOnly(libs.log4j.core)
    testCompileOnly(libs.log4j.core)
    compileOnly(libs.log4j.api)
}

description = "pinpoint-log4j2-plugin"
