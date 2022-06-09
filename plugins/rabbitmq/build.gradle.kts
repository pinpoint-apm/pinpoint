plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("org.springframework.amqp:spring-rabbit:1.7.6.RELEASE")
}

description = "pinpoint-rabbitmq-plugin"
