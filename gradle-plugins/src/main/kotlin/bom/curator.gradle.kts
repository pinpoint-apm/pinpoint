package com.navercorp.pinpoint.gradle.plugins.bom

plugins {
    `java-library`
}

dependencies {
    implementation("org.apache.curator:curator-framework:4.2.0") {
        exclude(group = "org.apache.zookeeper", module = "zookeeper")
        exclude(group = "org.apache.curator", module = "curator-test")
    }
}
