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
    implementation("com.google.guava:guava")
    implementation("com.google.inject:guice")
    implementation("org.apache.thrift:libthrift")
    implementation("org.apache.logging.log4j:log4j-core:${Versions.log4jJDK7}")
    implementation("com.google.protobuf:protobuf-java:${Versions.protoc}")
    implementation("io.grpc:grpc-stub:${Versions.grpc}")
    implementation("io.netty:netty")
    implementation("io.netty:netty-common:${Versions.netty4}")
    implementation("io.grpc:grpc-core:${Versions.grpc}")
    runtimeOnly("org.slf4j:slf4j-api:${Versions.slf4j}")
    testImplementation("com.google.inject.extensions:guice-grapher:4.1.0")
    testImplementation("commons-lang:commons-lang")
    testImplementation(project(":pinpoint-rpc"))
    testImplementation(project(":pinpoint-testcase"))
    testImplementation("org.springframework:spring-context:${Versions.spring4}")
    testImplementation("org.awaitility:awaitility")
}

description = "pinpoint-profiler"
