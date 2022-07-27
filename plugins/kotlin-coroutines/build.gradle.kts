plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-annotations"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
}

description = "pinpoint-kotlin-coroutines-plugin"
