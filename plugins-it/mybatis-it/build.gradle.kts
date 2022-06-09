plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    implementation(libs.spring4.beans)
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation(libs.mybatis)
    testImplementation(libs.mybatis.spring)
}

description = "pinpoint-mybatis-plugin-it"
