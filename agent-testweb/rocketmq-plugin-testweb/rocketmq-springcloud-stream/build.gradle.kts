plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.logging)
    implementation(libs.spring.boot.starter.actuator)
    implementation("org.springdoc:springdoc-openapi-ui:1.4.4")
    implementation("com.alibaba.cloud:spring-cloud-starter-stream-rocketmq:2.2.3.RELEASE")
    compileOnly(libs.spring.boot.starter.tomcat)
}

description = "pinpoint-rocketmq-springcloud-stream"
