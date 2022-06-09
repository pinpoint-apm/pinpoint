plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("org.apache.hbase:hbase-shaded-client:1.2.12")
}

description = "pinpoint-hbase-plugin-it"
