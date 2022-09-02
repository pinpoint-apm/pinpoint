plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    compileOnly("org.eclipse.paho:org.eclipse.paho.mqttv5.client:1.2.5")
}

description = "pinpoint-paho-mqtt-plugin"
