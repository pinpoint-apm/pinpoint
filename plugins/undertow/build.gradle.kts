plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    implementation(project(":pinpoint-common-servlet"))
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(libs.javax.servlet.api.v3)
    compileOnly("io.undertow:undertow-core:2.0.1.Final")
}

description = "pinpoint-undertow-plugin"
