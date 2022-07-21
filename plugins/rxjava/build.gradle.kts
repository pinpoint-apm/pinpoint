plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly("io.reactivex:rxjava:1.2.0")
}

description = "pinpoint-rxjava-plugin"
