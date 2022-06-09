plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(libs.netty.all) {
        exclude(group = "io.netty", module = "netty-codec-haproxy")
        exclude(group = "io.netty", module = "netty-codec-smtp")
        exclude(group = "io.netty", module = "netty-codec-stomp")
        exclude(group = "io.netty", module = "netty-codec-xml")
        exclude(group = "io.netty", module = "netty-codec-socks")
    }
}

description = "pinpoint-netty-plugin"