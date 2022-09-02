plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly("io.reactivex:rxjava:1.2.0")
}

description = "pinpoint-rxjava-plugin"
