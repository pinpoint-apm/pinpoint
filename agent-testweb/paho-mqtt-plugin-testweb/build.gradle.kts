plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.logging)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.springdoc.openapi.ui)
    compileOnly(libs.spring.boot.starter.tomcat)
    compileOnly("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    compileOnly("org.eclipse.paho:org.eclipse.paho.mqttv5.client:1.2.5")
}

description = "pinpoint-paho-mqtt-plugin-testweb"
