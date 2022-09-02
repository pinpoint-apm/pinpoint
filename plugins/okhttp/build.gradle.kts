plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly("com.squareup.okhttp:okhttp:2.5.0")
    compileOnly("com.squareup.okhttp3:okhttp:3.8.1")
}

description = "pinpoint-okhttp-plugin"
