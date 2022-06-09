plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("io.reactivex:rxjava:1.2.0")
}

description = "pinpoint-rxjava-plugin-it"
