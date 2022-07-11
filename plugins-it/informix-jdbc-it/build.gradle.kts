plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    api(project(":pinpoint-plugin-it-jdbc-test"))
    api(project(":pinpoint-bootstrap-core"))
    testImplementation("org.testcontainers:testcontainers:1.16.2")
    testImplementation("com.ibm.informix:jdbc:4.10.14")
    testImplementation(project(":pinpoint-informix-jdbc-driver-plugin"))
    testImplementation(project(":pinpoint-profiler-test"))
    testImplementation(libs.log4j.api)
}

description = "pinpoint-informix-jdbc-driver-plugin-it"
