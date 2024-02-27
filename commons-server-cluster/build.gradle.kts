plugins {
    id("com.navercorp.pinpoint.java11-library")
}

dependencies {
    implementation(libs.zookeeper)
    implementation(libs.spring.context)
    implementation(libs.spring.boot)
    implementation(libs.log4j.api)
    implementation(libs.curator.client) {
        exclude(group = "org.apache.zookeeper", module = "zookeeper")
        exclude(group = "org.apache.curator", module = "curator-test")
    }
    implementation(libs.curator.framework) {
        exclude(group = "org.apache.zookeeper", module = "zookeeper")
        exclude(group = "org.apache.curator", module = "curator-test")
        exclude(group = "org.apache.curator", module = "curator-client")
    }
    runtimeOnly(libs.slf4j.api)
    runtimeOnly(libs.log4j.slf4j.impl)
    runtimeOnly(libs.log4j.core)
    runtimeOnly(libs.log4j.jcl) {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    testImplementation(libs.spring.test)
    testImplementation(libs.curator.test)
    testImplementation(project(":pinpoint-testcase"))
    testImplementation(libs.awaitility)
}

description = "pinpoint-commons-server-cluster"
