plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation(project(":pinpoint-kotlin-coroutines-plugin"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
}

description = "pinpoint-kotlin-coroutines-plugin-it"
