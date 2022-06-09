plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("com.netflix.hystrix:hystrix-core:1.5.12")
}

description = "pinpoint-hystrix-plugin"
