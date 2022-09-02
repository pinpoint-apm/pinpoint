plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly(libs.spring.webflux)
    compileOnly(libs.spring.web)
}

description = "pinpoint-spring-webflux-plugin"
