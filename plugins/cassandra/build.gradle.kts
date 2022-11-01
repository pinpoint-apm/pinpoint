plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    testImplementation(libs.log4j.api)
    testImplementation(libs.log4j.slf4j.impl)
    testImplementation(libs.log4j.core)
    testCompileOnly(project(":pinpoint-test"))
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly("com.datastax.cassandra:cassandra-driver-core:2.1.7.1")
}

description = "pinpoint-cassandra-driver-plugin"
