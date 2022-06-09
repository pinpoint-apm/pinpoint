plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    testImplementation("org.eclipse.paho:org.eclipse.paho.mqttv5.client:1.2.5")
    testImplementation("org.testcontainers:testcontainers:1.16.2")
}

description = "pinpoint-paho-mqtt-plugin-it"
