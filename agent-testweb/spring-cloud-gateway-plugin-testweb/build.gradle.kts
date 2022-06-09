plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway:2.1.2.RELEASE")
    implementation("org.springframework.boot:spring-boot-autoconfigure:2.4.1")
}

description = "pinpoint-spring-cloud-gateway-plugin-testweb"
