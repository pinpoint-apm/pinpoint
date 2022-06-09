plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-commons"))
    implementation("org.nanohttpd:nanohttpd:2.3.1")
}

description = "pinpoint-plugin-it-utils"
