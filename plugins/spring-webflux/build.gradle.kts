plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(libs.spring.webflux)
    compileOnly(libs.spring.web)
}

description = "pinpoint-spring-webflux-plugin"
