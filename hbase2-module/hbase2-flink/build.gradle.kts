plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java11")
}

dependencies {
    api(project(":pinpoint-flink"))
    implementation("org.apache.hbase:hbase-client:2.4.2")
}

description = "pinpoint-hbase2-flink"
