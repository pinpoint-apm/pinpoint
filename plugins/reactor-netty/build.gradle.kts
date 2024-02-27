plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly(project(":pinpoint-common-servlet"))
    compileOnly("io.projectreactor.netty:reactor-netty:0.9.1.RELEASE")
}

description = "pinpoint-reactor-netty-plugin"
