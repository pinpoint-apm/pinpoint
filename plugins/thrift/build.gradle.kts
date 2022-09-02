plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    testImplementation(libs.log4j.api.jdk7)
    testImplementation(libs.log4j.slf4j.impl.jdk7)
    testImplementation(libs.log4j.core.jdk7)
    testImplementation(libs.log4j.jcl.jdk7)
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly(libs.libthrift.v012)
}

description = "pinpoint-thrift-plugin"
