plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation(libs.httpcore)
    testImplementation("org.apache.httpcomponents:httpasyncclient:4.1.4")
    testImplementation("org.apache.httpcomponents:httpcore-nio:4.4.14")
    testImplementation(libs.httpclient)
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
    testImplementation(libs.nanohttpd)
}

description = "pinpoint-httpclient4-plugin-it"
