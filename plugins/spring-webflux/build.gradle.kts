plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("org.springframework:spring-webflux:${Versions.spring}")
    compileOnly(libs.spring.web)
}

description = "pinpoint-spring-webflux-plugin"
