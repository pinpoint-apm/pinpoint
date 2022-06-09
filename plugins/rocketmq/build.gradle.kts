plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("org.apache.rocketmq:rocketmq-client:4.7.1")
}

description = "pinpoint-rocketmq-plugin"
