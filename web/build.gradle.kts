/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java11-conventions")
    id("pinpoint.plugins-assembly-conventions")
    id("pinpoint.agent-plugins-conventions")
}

dependencies {
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-commons-server"))
    api(project(":pinpoint-commons-server-cluster"))
    api(project(":pinpoint-commons-hbase"))
    api(project(":pinpoint-rpc"))
    api(project(":pinpoint-thrift"))
    api(project(":pinpoint-grpc"))
    implementation("com.google.guava:guava:30.1-jre")
    implementation("io.netty:netty:${Versions.netty3}")
    implementation("org.apache.zookeeper:zookeeper:3.4.14")
    implementation("org.apache.commons:commons-lang3:3.8.1")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.apache.thrift:libthrift:0.15.0")
    implementation("org.springframework:spring-core:5.3.13")
    implementation("org.springframework:spring-web:5.3.13")
    implementation("org.springframework:spring-webmvc:5.3.13")
    implementation("org.springframework:spring-websocket:5.3.13")
    implementation("org.springframework:spring-jdbc:5.3.13")
    implementation("org.springframework:spring-context:5.3.13")
    implementation("org.springframework:spring-context-support:5.3.13")
    implementation("org.springframework:spring-messaging:5.3.13")
    implementation("org.springframework.security:spring-security-web:5.5.3")
    implementation("org.springframework.security:spring-security-config:5.5.3")
    implementation("org.springframework.security:spring-security-messaging:5.5.3")
    implementation("org.springframework.boot:spring-boot-starter-web:2.5.7")
    implementation("org.springframework.boot:spring-boot-starter-log4j2:2.5.7")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.mybatis:mybatis:3.5.7")
    implementation("org.mybatis:mybatis-spring:2.0.6")
    implementation("mysql:mysql-connector-java:8.0.27")
    implementation("com.github.ben-manes.caffeine:caffeine:2.9.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.5")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.12.5")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.5")
    implementation("org.apache.logging.log4j:log4j-api:2.17.1")
    implementation("commons-codec:commons-codec:1.14")
    implementation("com.sun.mail:jakarta.mail:1.6.7")
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("org.aspectj:aspectjweaver:1.9.5")
    runtimeOnly("commons-lang:commons-lang:2.6")
    runtimeOnly("org.slf4j:slf4j-api:1.7.30")
    runtimeOnly("org.apache.logging.log4j:log4j-jcl:2.17.1")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.17.1")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.17.1")
    testImplementation("org.springframework:spring-test:5.3.13")
    testImplementation("com.jayway.jsonpath:json-path:1.2.0")
    testImplementation(project(":pinpoint-profiler"))
    testImplementation(project(":pinpoint-collector"))
    testImplementation("org.awaitility:awaitility:3.1.5")
    testImplementation("org.apache.curator:curator-test:2.13.0")
    testImplementation(project(":pinpoint-rpc"))
    testImplementation("org.skyscreamer:jsonassert:1.5.0")
    compileOnly("javax.servlet:javax.servlet-api:4.0.1")
    compileOnly("org.springframework.boot:spring-boot-starter-tomcat:2.5.7")
}

description = "pinpoint-web"
