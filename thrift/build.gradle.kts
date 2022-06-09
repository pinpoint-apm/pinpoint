plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-commons-buffer"))
    implementation(libs.libthrift.v012)
    implementation(libs.log4j.slf4j.impl.jdk7)
    implementation(libs.log4j.core.jdk7)
    implementation(libs.log4j.jcl.jdk7)
    runtimeOnly(libs.slf4j.api)
    testImplementation(libs.commons.lang)
}

description = "pinpoint-thrift"
