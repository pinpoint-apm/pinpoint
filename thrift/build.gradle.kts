plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(project(":pinpoint-commons"))
    implementation(project(":pinpoint-commons-buffer"))
    implementation(libs.libthrift)
    implementation(libs.log4j.slf4j.impl)
    implementation(libs.log4j.core)
    implementation(libs.log4j.jcl)
    runtimeOnly(libs.slf4j.api)
    testImplementation(libs.commons.lang)
}

description = "pinpoint-thrift"
