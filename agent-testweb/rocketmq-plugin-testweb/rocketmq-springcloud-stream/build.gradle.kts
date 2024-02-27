plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.logging)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.springdoc.openapi.ui)
    implementation("com.alibaba.cloud:spring-cloud-starter-stream-rocketmq:2.2.3.RELEASE")
    compileOnly(libs.spring.boot.starter.tomcat)
}

description = "pinpoint-rocketmq-springcloud-stream"
