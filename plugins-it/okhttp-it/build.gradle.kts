plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("com.squareup.okhttp:okhttp:2.5.0")
    testImplementation("com.squareup.okhttp3:okhttp:3.8.1")
    testImplementation(project(":pinpoint-okhttp-plugin"))
}

description = "pinpoint-okhttp-plugin-it"
