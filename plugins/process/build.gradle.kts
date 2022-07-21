plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    implementation(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    testCompileOnly(project(":pinpoint-commons"))
    compileOnly(project(":pinpoint-annotations"))
}

description = "pinpoint-process-plugin"
