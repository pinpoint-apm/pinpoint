plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnlyApi(project(":pinpoint-bootstrap-core"))
    compileOnly("commons-dbcp:commons-dbcp:1.4")
}

description = "pinpoint-commons-dbcp-plugin"
