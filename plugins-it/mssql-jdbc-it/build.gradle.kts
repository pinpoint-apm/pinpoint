plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    implementation(project(":pinpoint-plugin-it-utils"))
    implementation(project(":pinpoint-plugin-it-jdbc-test"))
    implementation(project(":pinpoint-bootstrap-core"))
    testImplementation("org.testcontainers:testcontainers:1.16.2")
    testImplementation("org.testcontainers:mssqlserver:1.16.2")
    testImplementation("com.microsoft.sqlserver:mssql-jdbc:7.0.0.jre8")
    testImplementation(project(":pinpoint-mssql-jdbc-driver-plugin"))
    testImplementation(project(":pinpoint-profiler-test"))
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
    testImplementation(libs.log4j.api)
    testCompileOnly(project(":pinpoint-profiler"))
}

description = "pinpoint-mssql-jdbc-driver-plugin-it"
