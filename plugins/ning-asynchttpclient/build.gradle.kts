plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly("com.ning:async-http-client:1.8.3")
    compileOnly("org.asynchttpclient:async-http-client:2.0.32")
}

description = "pinpoint-ning-asynchttpclient-plugin"
