plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java11")
}

dependencies {
    api(project(":pinpoint-collector"))
    implementation("org.apache.hbase:hbase-client:2.4.2")
    implementation(libs.spring.boot.autoconfigure)
}

description = "pinpoint-hbase2-collector"
