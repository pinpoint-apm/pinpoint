plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    api(project(":pinpoint-plugin-it-jdbc-test"))
    testImplementation(libs.mysql.connector.java)
    testImplementation(project(":pinpoint-mysql-jdbc-driver-plugin"))
    testImplementation("org.testcontainers:mysql:1.16.2")
    testImplementation(project(":pinpoint-profiler-test"))
}

description = "pinpoint-mysql-jdbc-driver-plugin-it"
