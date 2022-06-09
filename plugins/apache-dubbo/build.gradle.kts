plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("org.apache.dubbo:dubbo:2.7.2")
    testImplementation(project(":pinpoint-bootstrap-core"))
    testImplementation("org.apache.dubbo:dubbo:2.7.2")
}

description = "pinpoint-apache-dubbo-plugin"
