plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnlyApi(project(":pinpoint-bootstrap-core"))
    compileOnlyApi("org.apache.dubbo:dubbo:2.7.2")
}

description = "pinpoint-apache-dubbo-plugin"
