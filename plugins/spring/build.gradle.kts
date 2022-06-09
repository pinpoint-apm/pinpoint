plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    testImplementation(project(":pinpoint-test"))
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(libs.spring4.context)
    compileOnly(libs.spring4.web)
}

description = "pinpoint-spring-plugin"
