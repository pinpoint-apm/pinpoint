plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    testCompileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly(libs.netty.all) {
        exclude(group = "io.netty", module = "netty-codec-haproxy")
        exclude(group = "io.netty", module = "netty-codec-smtp")
        exclude(group = "io.netty", module = "netty-codec-stomp")
        exclude(group = "io.netty", module = "netty-codec-xml")
        exclude(group = "io.netty", module = "netty-codec-socks")
    }
}

description = "pinpoint-netty-plugin"