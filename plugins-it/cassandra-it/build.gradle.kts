plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    implementation(libs.guava.cassandra)
    testImplementation("com.datastax.cassandra:cassandra-driver-core:2.1.7.1")
    testImplementation("org.testcontainers:cassandra:1.16.2")
    testImplementation(project(":pinpoint-commons-profiler"))
    testImplementation(project(":pinpoint-plugin-it-utils"))
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
}

description = "pinpoint-cassandra-plugin-it"
