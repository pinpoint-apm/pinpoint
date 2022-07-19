plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    testCompileOnly(project(":pinpoint-bootstrap-core"))
    compileOnlyApi("org.apache.hbase:hbase-shaded-client:1.2.12")
    compileOnly(libs.commons.logging)
}

description = "pinpoint-hbase-plugin"
