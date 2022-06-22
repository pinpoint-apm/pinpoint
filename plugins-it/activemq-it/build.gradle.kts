plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    implementation(libs.slf4j.api)
    implementation(libs.log4j.slf4j.impl)
    testImplementation("org.apache.activemq:activemq-all:5.13.2") {
        exclude(group = "org.slf4j", module = "impl")
    }
    testImplementation(project(":pinpoint-profiler-test")) {
        exclude(group = "org.apache.logging.log4j", module = "log4j-slf4j-impl")
    }
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
}

description = "pinpoint-activemq-plugin-it"
