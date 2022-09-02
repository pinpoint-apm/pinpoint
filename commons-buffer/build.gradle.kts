plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(project(":pinpoint-commons"))
    testImplementation(libs.log4j.api.jdk7)
    testImplementation(libs.log4j.slf4j.impl.jdk7)
    testImplementation(libs.log4j.core.jdk7)
    testImplementation(libs.commons.lang)
}

description = "pinpoint-commons-buffer"
