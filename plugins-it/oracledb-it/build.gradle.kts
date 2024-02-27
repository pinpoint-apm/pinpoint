plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-plugin-it-jdbc-test"))
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation(project(":pinpoint-oracle-jdbc-driver-plugin"))
    testImplementation(libs.testcontainers.oraclexe)
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
    testImplementation(libs.log4j.api)
}

description = "pinpoint-oracle-jdbc-driver-plugin-it"
