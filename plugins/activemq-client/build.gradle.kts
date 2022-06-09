plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("org.apache.activemq:activemq-client:5.13.2")
}

description = "pinpoint-activemq-client-plugin"
