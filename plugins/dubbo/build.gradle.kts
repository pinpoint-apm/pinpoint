plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    testCompileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    testCompileOnly(project(":pinpoint-commons"))
    compileOnly("com.alibaba:dubbo:2.5.3")
}

description = "pinpoint-dubbo-plugin"
