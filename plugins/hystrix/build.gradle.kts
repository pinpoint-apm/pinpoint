plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly("com.netflix.hystrix:hystrix-core:1.5.12")
}

description = "pinpoint-hystrix-plugin"
