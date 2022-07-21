plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("com.google.http-client:google-http-client:1.20.0")
    compileOnly(project(":pinpoint-commons"))
}

description = "pinpoint-google-httpclient-plugin"
