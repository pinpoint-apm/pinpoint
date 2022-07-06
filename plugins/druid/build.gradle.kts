plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnlyApi(project(":pinpoint-bootstrap-core"))
    compileOnly("com.alibaba:druid:1.1.10")
}

description = "pinpoint-druid-plugin"
