plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(libs.snakeyaml)
    implementation(libs.log4j.api)
    testImplementation(libs.log4j.slf4j.impl)
    testImplementation(libs.log4j.core)
    compileOnly(project(":pinpoint-commons"))
    testCompileOnly(project(":pinpoint-commons"))
    compileOnly(project(":pinpoint-bootstrap-core"))
}

description = "pinpoint-plugins-loader"
