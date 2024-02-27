plugins {
    id("com.navercorp.pinpoint.java8-library")
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
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
    testImplementation(libs.nanohttpd)
}

description = "pinpoint-cxf-plugin-it"
