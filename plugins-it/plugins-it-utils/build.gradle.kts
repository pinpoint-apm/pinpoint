plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-commons"))
    implementation(libs.nanohttpd)
}

description = "pinpoint-plugin-it-utils"
