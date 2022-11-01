plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    testImplementation(project(":pinpoint-test"))
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly(libs.spring4.context)
    compileOnly(libs.spring4.web)
    testCompileOnly(libs.spring4.beans)
    testImplementation(libs.log4j.api)
}

description = "pinpoint-spring-plugin"
