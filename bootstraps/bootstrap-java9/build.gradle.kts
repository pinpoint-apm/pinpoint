plugins {
    id("com.navercorp.pinpoint.java11-library")
}

dependencies {
    api(project(":pinpoint-bootstrap-core"))
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-bootstrap-java9-internal"))
    testImplementation(libs.log4j.api.jdk7)
    testImplementation(libs.log4j.slf4j.impl.jdk7)
    testImplementation(libs.log4j.core.jdk7)
    testImplementation(libs.log4j.jcl.jdk7)
    testImplementation(libs.mysql.connector.java)
    testImplementation(libs.commons.io)
}

description = "pinpoint-bootstrap-java9"

tasks.compileJava {
    options.release.set(9)
}