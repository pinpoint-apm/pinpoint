plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-jdbc-test"))
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation(project(":pinpoint-oracle-jdbc-driver-plugin"))
    testImplementation("org.testcontainers:oracle-xe:1.16.2")
}

description = "pinpoint-oracle-jdbc-driver-plugin-it"
