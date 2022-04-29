package com.navercorp.pinpoint.gradle.plugins.bom

plugins {
    `java-library`
}

dependencies {
    implementation("org.apache.hbase:hbase-shaded-client:1.7.1") {
        exclude("org.slf4j:slf4j-log4j12")
        exclude("commons-logging:commons-logging")
    }
    implementation("com.sematext.hbasewd:hbasewd:0.1.0") {
        exclude("log4j:log4j")
    }
}
