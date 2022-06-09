plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("org.springframework.amqp:spring-rabbit:1.7.6.RELEASE")
    testImplementation("org.apache.qpid:qpid-broker:6.1.1")
}

description = "pinpoint-rabbitmq-plugin-it"
