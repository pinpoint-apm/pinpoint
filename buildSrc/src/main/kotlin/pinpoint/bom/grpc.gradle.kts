package pinpoint.bom

plugins {
    `java-library`
}

dependencies {
    implementation("io.grpc:grpc-core:${Versions.grpc}")
    implementation("io.grpc:grpc-netty:${Versions.grpc}")
    implementation("io.netty:netty-handler:${Versions.netty4}")
    implementation("io.netty:netty-transport-native-epoll:${Versions.netty4}")
    implementation("io.netty:netty-codec-http2:${Versions.netty4}")
    implementation("io.grpc:grpc-stub:${Versions.grpc}")
    implementation("io.grpc:grpc-protobuf:${Versions.grpc}")
}
