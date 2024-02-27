plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly(libs.slf4j.api)
    compileOnly("ch.qos.logback:logback-core:1.2.5")
}

description = "pinpoint-logback-plugin"
