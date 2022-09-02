plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation(libs.spring4.test)
    testImplementation(libs.spring4.context)
    testImplementation(libs.spring4.webmvc)
    testImplementation(libs.javax.servlet.api.v3)
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
    testImplementation(libs.nanohttpd)
}

description = "pinpoint-spring-plugin-it"
