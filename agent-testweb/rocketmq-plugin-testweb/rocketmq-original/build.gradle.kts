plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.logging)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.springdoc.openapi.ui)
    implementation("org.apache.rocketmq:rocketmq-client:4.7.1")
    compileOnly(libs.spring.boot.starter.tomcat)
}

description = "pinpoint-rocketmq-original"
