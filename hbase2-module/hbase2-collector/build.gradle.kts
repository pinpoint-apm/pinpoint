plugins {
    id("com.navercorp.pinpoint.java11-library")
}

dependencies {
    implementation(project(":pinpoint-commons-server"))
    implementation(project(":pinpoint-collector"))
    implementation(libs.hbase.client) {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
        exclude(group = "org.slf4j", module = "slf4j-log4j12")
        exclude(group = "javax.annotation", module = "javax.annotation-api")
        exclude(group = "javax.activation", module = "javax.activation-api")
        exclude(group = "org.apache.curator", module = "curator-client")
        exclude(group = "org.apache.curator", module = "curator-framework")
    }
    implementation(libs.spring.boot.autoconfigure)
}

description = "pinpoint-hbase2-collector"
