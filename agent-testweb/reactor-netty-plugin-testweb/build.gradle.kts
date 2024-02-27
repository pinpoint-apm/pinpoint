plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.logging)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.springdoc.openapi.ui)
    implementation("io.projectreactor.netty:reactor-netty:0.9.12.RELEASE")
    compileOnly(libs.spring.boot.starter.tomcat)
}

description = "pinpoint-reactor-netty-plugin-testweb"
