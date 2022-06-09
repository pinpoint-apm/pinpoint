plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java11")
}

dependencies {
    api(project(":pinpoint-hbase-schema"))
    implementation(libs.spring.boot.configuration.processor)
    implementation(libs.commons.lang3)
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.log4j2)
    implementation(libs.spring.tx)
    implementation("org.apache.logging.log4j:log4j-1.2-api:2.14.1")
    testImplementation(libs.spring.boot.starter.test)

    implementation(libs.hbase.shaded.client) {
        exclude(group = "org.slf4j", module = "slf4j-log4j12")
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation(libs.hbasewd) {
        exclude(group = "log4j", module = "log4j")
    }
}

description = "pinpoint-hbase-schema-manager"
