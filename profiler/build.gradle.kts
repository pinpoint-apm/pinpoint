plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(project(":pinpoint-annotations"))
    implementation(project(":pinpoint-commons"))
    implementation(project(":pinpoint-commons-buffer"))
    implementation(project(":pinpoint-commons-profiler"))
    implementation(project(":pinpoint-thrift"))
    implementation(project(":pinpoint-grpc"))
    implementation(project(":pinpoint-bootstrap-core"))
    implementation(project(":pinpoint-bootstrap"))
    implementation(project(":pinpoint-rpc"))
    implementation(project(":pinpoint-plugins-loader"))
    implementation(project(":pinpoint-grpc"))
    implementation(project(":pinpoint-profiler-logging"))
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
    testImplementation(testFixtures(project(":pinpoint-rpc")))
}

description = "pinpoint-profiler"
