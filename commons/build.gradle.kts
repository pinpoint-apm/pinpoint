plugins {
    id("com.navercorp.pinpoint.java8-library")
}


dependencies {
    testImplementation(libs.log4j.api)
    testImplementation(libs.log4j.slf4j.impl)
    testImplementation(libs.log4j.core)
    testImplementation(libs.commons.lang3)
}

description = "pinpoint-commons"