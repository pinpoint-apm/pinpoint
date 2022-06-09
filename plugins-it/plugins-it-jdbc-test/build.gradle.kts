plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-bootstrap-core"))
    implementation(libs.log4j.api.jdk7)
    implementation(libs.junit)
}

description = "pinpoint-plugin-it-jdbc-test"
