plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("ch.qos.logback:logback-classic:1.2.3")
}

description = "pinpoint-logback-plugin-it"
