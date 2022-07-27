plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation("com.ning:async-http-client:1.8.3")
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
    testImplementation(libs.nanohttpd)
}

description = "pinpoint-ning-aysncclient-plugin-it"
