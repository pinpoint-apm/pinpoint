plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("com.squareup.okhttp:okhttp:2.5.0")
    testImplementation("com.squareup.okhttp3:okhttp:3.8.1")
    testImplementation(project(":pinpoint-okhttp-plugin"))
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
    testImplementation(libs.nanohttpd)
}

description = "pinpoint-okhttp-plugin-it"
