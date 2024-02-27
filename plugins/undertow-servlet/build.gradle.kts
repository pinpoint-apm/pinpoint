plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(project(":pinpoint-common-servlet"))
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly(libs.javax.servlet.api.v3)
    compileOnly("io.undertow:undertow-core:2.0.1.Final")
}

description = "pinpoint-undertow-servlet-plugin"
