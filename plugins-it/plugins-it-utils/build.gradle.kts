plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-commons"))
    implementation(libs.nanohttpd)
}

description = "pinpoint-plugin-it-utils"
