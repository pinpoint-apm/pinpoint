import com.google.protobuf.gradle.*

plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java7")
    alias(libs.plugins.protobuf)
}

dependencies {
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-bootstrap-core"))
    api(project(":pinpoint-commons-profiler"))
    implementation(libs.log4j.slf4j.impl.jdk7)
    implementation(libs.log4j.core.jdk7)
    implementation(libs.commons.lang3)
    runtimeOnly("io.netty:netty-tcnative-boringssl-static:2.0.38.Final")
    runtimeOnly(libs.slf4j.api)
    testImplementation(libs.grpc.testing)
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

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

sourceSets {
    main {
        proto {
            srcDir("grpc-idl/proto")
        }
    }
}

protobuf {
    val protocVersion = libs.versions.protoc.get()
    val grpcVersion = libs.versions.grpc.get()
    protoc {
        artifact = "com.google.protobuf:protoc:${protocVersion}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                // Apply the "grpc" plugin whose spec is defined above, without options.
                id("grpc")
            }
        }
    }
}

description = "pinpoint-grpc"
