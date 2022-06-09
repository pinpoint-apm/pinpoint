plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("org.apache.commons:commons-dbcp2:2.1.1")
}

description = "pinpoint-commons-dbcp2-plugin"
