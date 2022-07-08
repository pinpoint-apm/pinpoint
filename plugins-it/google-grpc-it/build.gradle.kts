import com.google.protobuf.gradle.*

plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
    alias(libs.plugins.protobuf)
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("io.grpc:grpc-protobuf:1.14.0")
    testImplementation("io.grpc:grpc-stub:1.14.0")
    testImplementation("io.grpc:grpc-netty:1.14.0")
    testImplementation(libs.netty.all)
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
}

sourceSets {
    test {
        proto {
            srcDir("test/proto")
        }
    }
}

protobuf {
    val protocVersion = libs.versions.protoc.get()
    val grpcVersion = libs.versions.grpc.get()
    protoc {
        artifact = "com.google.protobuf:protoc:3.5.1-1"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.14.0"
        }
    }
    generateProtoTasks {
        ofSourceSet("test").forEach {
            it.plugins {
                // Apply the "grpc" plugin whose spec is defined above, without options.
                id("grpc")
            }
        }
    }
}


description = "pinpoint-grpc-plugin-it"
