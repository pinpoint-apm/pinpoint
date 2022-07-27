plugins {
    kotlin("jvm") version "1.7.10"
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation(project(":pinpoint-kotlin-coroutines-plugin"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
}

description = "pinpoint-kotlin-coroutines-plugin-it"
