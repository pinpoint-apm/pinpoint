plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly("org.apache.rocketmq:rocketmq-client:4.7.1")
}

description = "pinpoint-rocketmq-plugin"
