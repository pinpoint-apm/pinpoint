plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(project(":pinpoint-plugin-it-utils"))
    implementation(project(":pinpoint-plugin-it-jdbc-test"))
    implementation(project(":pinpoint-bootstrap-core"))
    testImplementation(libs.testcontainers)
    testImplementation("com.ibm.informix:jdbc:4.10.14")
    testImplementation(project(":pinpoint-informix-jdbc-driver-plugin"))
    testImplementation(project(":pinpoint-profiler"))
    testImplementation(project(":pinpoint-profiler-test"))
    testImplementation(libs.log4j.api)
}

description = "pinpoint-informix-jdbc-driver-plugin-it"
