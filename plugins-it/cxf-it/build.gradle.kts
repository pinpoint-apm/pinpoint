plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("org.apache.cxf:cxf-rt-frontend-jaxrs:3.0.16") {
        exclude(group = "javax.annotation", module = "javax.annotation-api")
    }
    testImplementation("org.apache.cxf:cxf-rt-frontend-jaxws:3.0.16") {
        exclude(group = "javax.annotation", module = "javax.annotation-api")
        exclude(group = "org.apache.cxf", module = "cxf-rt-databinding-jaxb")
    }
    testImplementation("org.apache.cxf:cxf-rt-transports-http:3.0.16")
    testImplementation("org.apache.cxf:cxf-rt-rs-client:3.0.16")
    testImplementation(project(":pinpoint-bootstrap-core"))
    testImplementation(project(":pinpoint-test"))
    testImplementation(libs.nanohttpd)
}

description = "pinpoint-cxf-plugin-it"
