plugins {
    id("com.navercorp.pinpoint.java11-library")
}

dependencies {
    implementation(project(":pinpoint-commons"))
    implementation(project(":pinpoint-commons-hbase"))
    implementation(project(":pinpoint-commons-server"))
    implementation(project(":pinpoint-commons-server-cluster"))
    implementation(project(":pinpoint-thrift"))
    implementation(project(":pinpoint-rpc"))
    implementation(project(":pinpoint-grpc"))
    implementation(libs.libthrift) {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "org.apache.httpcomponents", module = "httpcore")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "javax.annotation", module = "javax.annotation-api")
    }
    implementation(project(":pinpoint-web"))
    implementation(project(":pinpoint-collector"))
    implementation(project(":pinpoint-commons-profiler"))
    implementation(libs.spring.core) {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation(libs.spring.context)
    implementation(libs.spring.context.support)
    implementation(libs.jakarta.annotation.api)
    implementation(libs.caffeine)
    implementation(libs.log4j.api)
    implementation(libs.metrics.core)
    implementation(libs.metrics.jvm)
    implementation(libs.metrics.servlets) {
        exclude(group = "com.papertrail", module = "profiler")
    }
    implementation(libs.zookeeper)
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
    testImplementation(libs.log4j.jcl) {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    testImplementation(libs.log4j.slf4j.impl)
    testImplementation(libs.log4j.core)
    testImplementation(libs.spring.test)
    compileOnly("org.apache.flink:flink-java:1.14.2")
    testCompileOnly("org.apache.flink:flink-java:1.14.2")
    compileOnly("org.apache.flink:flink-streaming-java_2.11:1.14.2")
    compileOnly("org.apache.flink:flink-clients_2.11:1.14.2")

    implementation(libs.hbase.shaded.client) {
        exclude(group = "org.slf4j", module = "slf4j-log4j12")
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation(libs.hbasewd) {
        exclude(group = "log4j", module = "log4j")
    }
}

description = "pinpoint-flink"
