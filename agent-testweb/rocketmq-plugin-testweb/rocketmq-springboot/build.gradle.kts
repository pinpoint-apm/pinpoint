plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.logging)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.springdoc.openapi.ui)
    implementation("org.apache.rocketmq:rocketmq-spring-boot-starter:2.1.1")
    compileOnly(libs.spring.boot.starter.tomcat)
}

description = "pinpoint-rocketmq-springboot"
