plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly("org.springframework.amqp:spring-rabbit:1.7.6.RELEASE")
}

description = "pinpoint-rabbitmq-plugin"
