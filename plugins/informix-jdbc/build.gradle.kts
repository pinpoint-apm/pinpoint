plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    testImplementation(libs.log4j.api.jdk7)
    testImplementation(libs.log4j.slf4j.impl.jdk7)
    testImplementation(libs.log4j.core.jdk7)
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("com.ibm.informix:jdbc:4.10.10.0")
}

description = "pinpoint-informix-jdbc-driver-plugin"
