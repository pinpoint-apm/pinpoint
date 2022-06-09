plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("com.alibaba:dubbo:2.5.3")
}

description = "pinpoint-dubbo-plugin"
