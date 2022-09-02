plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    api(project(":pinpoint-plugin-it-jdbc-test"))
    testImplementation("net.sourceforge.jtds:jtds:1.2.8")
    testImplementation(project(":pinpoint-jtds-plugin"))
    testImplementation("org.testcontainers:mssqlserver:1.16.2")
    testImplementation(project(":pinpoint-profiler-test"))
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
    testImplementation(libs.log4j.api)
}

description = "pinpoint-jtds-plugin-it"
