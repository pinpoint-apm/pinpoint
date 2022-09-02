plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("org.springframework.amqp:spring-rabbit:1.7.6.RELEASE")
    testImplementation("org.apache.qpid:qpid-broker:6.1.1")
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
}

description = "pinpoint-rabbitmq-plugin-it"
