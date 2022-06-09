plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    implementation("com.ning:async-http-client:1.8.3")
    api(project(":pinpoint-plugin-it-utils"))
}

description = "pinpoint-ning-aysncclient-plugin-it"
