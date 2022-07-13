plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnlyApi(project(":pinpoint-bootstrap-core"))
    compileOnly(libs.spring4.web)
}

description = "pinpoint-resttemplate-plugin"
