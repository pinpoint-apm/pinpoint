plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    api(project(":pinpoint-plugin-it-jdbc-test"))
    api(project(":pinpoint-bootstrap-core"))
    testImplementation("org.testcontainers:testcontainers:1.16.2")
    testImplementation("org.testcontainers:mssqlserver:1.16.2")
    testImplementation("com.microsoft.sqlserver:mssql-jdbc:7.0.0.jre8")
    testImplementation(project(":pinpoint-mssql-jdbc-driver-plugin"))
    testImplementation(project(":pinpoint-profiler-test"))
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
    testImplementation(libs.log4j.api)
}

description = "pinpoint-mssql-jdbc-driver-plugin-it"
