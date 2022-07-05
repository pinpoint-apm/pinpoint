plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("org.apache.cxf:cxf-rt-frontend-jaxrs:3.0.16") {
        exclude(group = "javax.annotation", module = "javax.annotation-api")
    }
    compileOnly("org.apache.cxf:cxf-rt-frontend-jaxws:3.0.16") {
        exclude(group = "javax.annotation", module = "javax.annotation-api")
        exclude(group = "org.apache.cxf", module = "cxf-rt-databinding-jaxb")
    }
    compileOnly("org.apache.cxf:cxf-rt-transports-http:3.0.16")
    compileOnly("org.apache.cxf:cxf-rt-rs-client:3.0.16")
}

description = "pinpoint-cxf-plugin"
