plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    api(project(":pinpoint-plugin-it-jdbc-test"))
    testImplementation(libs.mysql.connector.java)
    testImplementation(project(":pinpoint-mysql-jdbc-driver-plugin"))
    testImplementation("org.testcontainers:mysql:1.16.2")
    testImplementation(project(":pinpoint-profiler-test"))
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
    testImplementation(libs.log4j.api)
}

description = "pinpoint-mysql-jdbc-driver-plugin-it"
