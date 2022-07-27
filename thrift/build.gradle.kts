plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(project(":pinpoint-commons"))
    implementation(project(":pinpoint-commons-buffer"))
    implementation(libs.libthrift.v012)
    implementation(libs.log4j.slf4j.impl.jdk7)
    implementation(libs.log4j.core.jdk7)
    implementation(libs.log4j.jcl.jdk7)
    runtimeOnly(libs.slf4j.api)
    testImplementation(libs.commons.lang)
}

description = "pinpoint-thrift"
