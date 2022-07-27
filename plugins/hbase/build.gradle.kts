plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    testCompileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    testCompileOnly(project(":pinpoint-commons"))
    compileOnly("org.apache.hbase:hbase-shaded-client:1.2.12")
    testCompileOnly("org.apache.hbase:hbase-shaded-client:1.2.12")
    compileOnly(libs.commons.logging)
}

description = "pinpoint-hbase-plugin"
