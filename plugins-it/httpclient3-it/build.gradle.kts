plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("commons-httpclient:commons-httpclient:3.1")
}

description = "pinpoint-httpclient3-plugin-it"
