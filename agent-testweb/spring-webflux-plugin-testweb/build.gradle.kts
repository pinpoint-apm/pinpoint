plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux:2.4.1")
    implementation("org.springframework.boot:spring-boot-autoconfigure:2.4.1")
}

description = "pinpoint-spring-webflux-plugin-testweb"
