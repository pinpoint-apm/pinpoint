plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java7")
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
    implementation("com.google.guava:guava")
    implementation("com.google.inject:guice")
    implementation(libs.libthrift.v012)
    implementation(libs.log4j.core.jdk7)
    implementation("com.google.protobuf:protobuf-java:${Versions.protoc}")
    implementation("io.grpc:grpc-stub:${Versions.grpc}")
    implementation(libs.netty)
    implementation(libs.netty.common)
    implementation("io.grpc:grpc-core:${Versions.grpc}")
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
