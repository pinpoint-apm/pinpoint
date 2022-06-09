plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java11")
}

dependencies {
    api(project(":pinpoint-hbase-schema-definition"))
    api(project(":pinpoint-commons-hbase"))
    api(project(":pinpoint-commons-buffer"))
    implementation(libs.spring.core) {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation(libs.log4j.api)
    implementation(libs.jakarta.bind.api)
    runtimeOnly(libs.jakarta.jaxb.impl)

    implementation(libs.hbase.shaded.client) {
        exclude(group = "org.slf4j", module = "slf4j-log4j12")
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation(libs.hbasewd) {
        exclude(group = "log4j", module = "log4j")
    }
}

description = "pinpoint-hbase-schema"
