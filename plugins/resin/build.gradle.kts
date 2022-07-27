plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(project(":pinpoint-common-servlet"))
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly(libs.javax.servlet.api.v3)
    compileOnly("com.caucho:resin:3.0.9")
}

description = "pinpoint-resin-plugin"
