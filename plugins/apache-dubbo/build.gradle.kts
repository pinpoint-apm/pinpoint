plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    testCompileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    testCompileOnly(project(":pinpoint-commons"))
    compileOnly("org.apache.dubbo:dubbo:2.7.2")
    testCompileOnly("org.apache.dubbo:dubbo:2.7.2")
}

description = "pinpoint-apache-dubbo-plugin"
