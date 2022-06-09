plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-annotations"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
}

description = "pinpoint-kotlin-coroutines-plugin"
