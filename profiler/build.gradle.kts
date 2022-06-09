plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-annotations"))
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-commons-buffer"))
    api(project(":pinpoint-commons-profiler"))
    api(project(":pinpoint-thrift"))
    api(project(":pinpoint-grpc"))
    api(project(":pinpoint-bootstrap-core"))
    api(project(":pinpoint-bootstrap"))
    api(project(":pinpoint-rpc"))
    api(project(":pinpoint-plugins-loader"))
    api(project(":pinpoint-grpc"))
    api(project(":pinpoint-profiler-logging"))
    implementation(libs.guava.jdk7) {
        exclude(group = "com.google.guava", module = "listenablefuture")
    }
    implementation(libs.guice)
    implementation(libs.libthrift.v012)
    implementation(libs.log4j.core.jdk7)
    implementation(libs.protobuf.java)
    implementation(libs.grpc.stub)
    implementation(libs.netty)
    implementation(libs.netty.common)
    implementation(libs.grpc.core) {
        exclude(group = "io.opencensus", module = "opencensus-api")
        exclude(group = "io.opencensus", module = "opencensus-contrib-grpc-metrics")
        exclude(group = "com.google.code.findbugs", module = "jsr305")
    }
    implementation(libs.bundles.asm)
    runtimeOnly(libs.slf4j.api)
    testImplementation("com.google.inject.extensions:guice-grapher:4.1.0")
    testImplementation(libs.commons.lang)
    testImplementation(project(":pinpoint-rpc"))
    testImplementation(project(":pinpoint-testcase"))
    testImplementation(libs.spring4.context)
    testImplementation(libs.awaitility)
}

description = "pinpoint-profiler"
