plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java11")
}

dependencies {
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-commons-buffer"))
    api(project(":pinpoint-commons-profiler"))
    api(project(":pinpoint-commons-server"))
    api(project(":pinpoint-commons-server-cluster"))
    api(project(":pinpoint-commons-hbase"))
    api(project(":pinpoint-rpc"))
    api(project(":pinpoint-thrift"))
    api(project(":pinpoint-grpc"))
    api(platform(project(":pinpoint-plugins")))
    api(project(":pinpoint-profiler"))

    implementation(libs.zookeeper)
    implementation(libs.guava.jdk8)
    implementation(libs.netty)
    implementation(libs.commons.lang3)
    implementation(libs.commons.collections4)
    implementation(libs.libthrift)
    implementation(libs.jackson.core)
    implementation(libs.jackson.annotations)
    implementation(libs.jackson.databind)
    implementation(libs.slf4j.api)
    implementation(libs.log4j.slf4j.impl)
    implementation(libs.spring.core) {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation(libs.spring.context)
    implementation(libs.spring.orm)
    implementation(libs.spring.web)
    implementation(libs.spring.webmvc)
    implementation("org.springframework.boot:spring-boot-starter-web:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-log4j2:${Versions.springBoot}")
    implementation(libs.metrics.core)
    implementation(libs.metrics.jvm)
    implementation(libs.metrics.servlets) {
        exclude(group = "com.papertrail", module = "profiler")
    }
    runtimeOnly(libs.commons.lang)
    runtimeOnly(libs.log4j.jcl)
    runtimeOnly(libs.log4j.core)
    testImplementation(libs.spring.test)
    testImplementation("org.springframework.boot:spring-boot-test:${Versions.springBoot}")
    testImplementation(libs.awaitility)
    testImplementation(project(":pinpoint-rpc"))
    compileOnly(libs.javax.servlet.api)

    implementation(libs.hbase.shaded.client) {
        exclude(group = "org.slf4j", module = "slf4j-log4j12")
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation(libs.hbasewd) {
        exclude(group = "log4j", module = "log4j")
    }
    implementation(libs.curator.framework) {
        exclude(group = "org.apache.zookeeper", module = "zookeeper")
        exclude(group = "org.apache.curator", module = "curator-test")
    }

    implementation(libs.grpc.core) {
        exclude(group = "io.opencensus", module = "opencensus-api")
        exclude(group = "io.opencensus", module = "opencensus-contrib-grpc-metrics")
        exclude(group = "com.google.code.findbugs", module = "jsr305")
    }
    implementation(libs.grpc.netty) {
        exclude(group = "io.netty", module = "netty-codec-http2")
        exclude(group = "io.netty", module = "netty-handler-proxy")
    }
    implementation(libs.netty.handler)
    implementation(libs.netty.transport.native.epoll)
    implementation(libs.netty.codec.http2)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.protobuf)
}

description = "pinpoint-collector"
