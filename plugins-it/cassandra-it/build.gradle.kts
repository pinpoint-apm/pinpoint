plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    testImplementation("com.datastax.cassandra:cassandra-driver-core:2.1.7.1")
    testImplementation(libs.testcontainers.cassandra)
    testImplementation(project(":pinpoint-commons-profiler"))
    testImplementation(project(":pinpoint-plugin-it-utils"))
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
}

description = "pinpoint-cassandra-plugin-it"
