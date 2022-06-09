plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation(libs.spring4.test)
    testImplementation(libs.spring4.context)
    testImplementation(libs.spring4.webmvc)
    testImplementation(libs.javax.servlet.api.v3)
}

description = "pinpoint-spring-plugin-it"
