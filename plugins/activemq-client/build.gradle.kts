plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly("org.apache.activemq:activemq-client:5.13.2")
}

description = "pinpoint-activemq-client-plugin"
