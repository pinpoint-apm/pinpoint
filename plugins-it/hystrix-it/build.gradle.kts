plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("com.netflix.hystrix:hystrix-core:1.5.12")
}

description = "pinpoint-hystrix-plugin-it"
