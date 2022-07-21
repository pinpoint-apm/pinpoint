plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly("io.projectreactor:reactor-core:3.3.0.RELEASE")
}

description = "pinpoint-reactor-plugin"
