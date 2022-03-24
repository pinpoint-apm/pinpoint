plugins {
    id("pinpoint.java7-conventions")
    id("pinpoint.asm-conventions")
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
    implementation("com.google.guava:guava:30.1-android")
    implementation("com.google.inject:guice:4.2.2")
    implementation("org.apache.thrift:libthrift:0.12.0")
    implementation("org.apache.logging.log4j:log4j-core:2.12.4")
    implementation("com.google.protobuf:protobuf-java:${Versions.protoc}")
    implementation("io.grpc:grpc-stub:${Versions.grpc}")
    implementation("io.netty:netty:${Versions.netty3}")
    implementation("io.netty:netty-common:${Versions.netty4}")
    implementation("io.grpc:grpc-core:${Versions.grpc}")
    runtimeOnly("org.slf4j:slf4j-api:1.7.30")
    testImplementation("com.google.inject.extensions:guice-grapher:4.1.0")
    testImplementation("commons-lang:commons-lang:2.6")
    testImplementation(project(":pinpoint-rpc"))
    testImplementation(project(":pinpoint-testcase"))
    testImplementation("org.springframework:spring-context:4.3.30.RELEASE")
    testImplementation("org.awaitility:awaitility:3.1.5")
}

description = "pinpoint-profiler"
