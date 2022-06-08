plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java7")
}

dependencies {
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-commons-buffer"))
    implementation(libs.log4j.api.jdk7)
    testImplementation(libs.log4j.slf4j.impl.jdk7)
    testImplementation(libs.log4j.core.jdk7)
    testImplementation(libs.log4j.jcl.jdk7)
    testImplementation(libs.commons.lang3)
}

description = "pinpoint-commons-profiler"
