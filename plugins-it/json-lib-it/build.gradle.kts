plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("net.sf.json-lib:json-lib:2.3:jdk15")
}

description = "pinpoint-json-lib-plugin-it"
