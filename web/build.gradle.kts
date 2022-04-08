/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java11-conventions")
    id("pinpoint.plugins-assembly-conventions")
    id("pinpoint.agent-plugins-conventions")
    id("pinpoint.hbase-conventions")
    id("pinpoint.curator-conventions")
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
    implementation("io.netty:netty")
    implementation("org.apache.zookeeper:zookeeper")
    implementation("org.apache.commons:commons-lang3")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.apache.commons:commons-collections4")
    implementation("org.apache.thrift:libthrift:0.15.0")
    implementation("org.springframework:spring-core")
    implementation("org.springframework:spring-web")
    implementation("org.springframework:spring-webmvc")
    implementation("org.springframework:spring-websocket")
    implementation("org.springframework:spring-jdbc")
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-context-support")
    implementation("org.springframework:spring-messaging:${Versions.spring}")
    implementation("org.springframework.security:spring-security-web:5.5.3")
    implementation("org.springframework.security:spring-security-config:5.5.3")
    implementation("org.springframework.security:spring-security-messaging:5.5.3")
    implementation("org.springframework.boot:spring-boot-starter-web:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-log4j2:${Versions.springBoot}")
    implementation("com.zaxxer:HikariCP")
    implementation("org.mybatis:mybatis")
    implementation("org.mybatis:mybatis-spring")
    implementation("mysql:mysql-connector-java")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.apache.logging.log4j:log4j-api:${Versions.log4jJDK8}")
    implementation("commons-codec:commons-codec")
    implementation("com.sun.mail:jakarta.mail")
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("org.aspectj:aspectjweaver:1.9.5")
    runtimeOnly("commons-lang:commons-lang")
    runtimeOnly("org.slf4j:slf4j-api:${Versions.slf4j}")
    runtimeOnly("org.apache.logging.log4j:log4j-jcl:${Versions.log4jJDK8}")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:${Versions.log4jJDK8}")
    runtimeOnly("org.apache.logging.log4j:log4j-core:${Versions.log4jJDK8}")
    testImplementation("org.springframework:spring-test:${Versions.spring}")
    testImplementation("com.jayway.jsonpath:json-path")
    testImplementation(project(":pinpoint-profiler"))
    testImplementation(project(":pinpoint-collector"))
    testImplementation("org.awaitility:awaitility")
    testImplementation("org.apache.curator:curator-test")
    testImplementation(project(":pinpoint-rpc"))
    testImplementation("org.skyscreamer:jsonassert:1.5.0")
    compileOnly("javax.servlet:javax.servlet-api:4.0.1")
    compileOnly("org.springframework.boot:spring-boot-starter-tomcat:${Versions.springBoot}")
}

description = "pinpoint-web"
