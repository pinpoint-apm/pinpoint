plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-common-servlet"))
    compileOnly("io.projectreactor.netty:reactor-netty:0.9.1.RELEASE")
}

description = "pinpoint-reactor-netty-plugin"
