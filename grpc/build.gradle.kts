import com.google.protobuf.gradle.*

plugins {
    id("pinpoint.toolchain.java7")
    id("pinpoint.bom.grpc")
    id("com.google.protobuf") version Versions.protobufPlugin
}

dependencies {
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-bootstrap-core"))
    api(project(":pinpoint-commons-profiler"))
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:${Versions.log4jJDK7}")
    implementation("org.apache.logging.log4j:log4j-core:${Versions.log4jJDK7}")
    implementation("org.apache.commons:commons-lang3")
    runtimeOnly("io.netty:netty-tcnative-boringssl-static:2.0.38.Final")
    runtimeOnly("org.slf4j:slf4j-api:${Versions.slf4j}")
    testImplementation("io.grpc:grpc-testing:${Versions.grpc}")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}

sourceSets {
    main {
        proto {
            srcDir("grpc-idl/proto")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${Versions.protoc}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${Versions.grpc}"
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
