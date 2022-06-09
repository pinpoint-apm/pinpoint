plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    testImplementation(libs.spring.context)
    testImplementation(project(":pinpoint-plugin-it-utils"))
}

description = "pinpoint-lambda-it"
