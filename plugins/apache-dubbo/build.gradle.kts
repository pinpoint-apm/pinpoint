plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    testCompileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("org.apache.dubbo:dubbo:2.7.2")
    testCompileOnly("org.apache.dubbo:dubbo:2.7.2")
}

description = "pinpoint-apache-dubbo-plugin"
