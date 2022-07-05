plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-commons"))
    compileOnlyApi(project(":pinpoint-bootstrap-core"))
    compileOnlyApi(libs.javax.servlet.api.v3)
}

description = "pinpoint-common-servlet"
