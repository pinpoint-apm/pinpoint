plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java11")
    id("org.siouan.frontend-jdk8") version "6.0.0"
}

dependencies {
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-commons-server"))
    api(project(":pinpoint-commons-server-cluster"))
    api(project(":pinpoint-commons-hbase"))
    api(project(":pinpoint-rpc"))
    api(project(":pinpoint-thrift"))
    api(project(":pinpoint-grpc"))
    api(platform(project(":pinpoint-plugins")))
    api(platform(project(":pinpoint-agent-plugins")))

    implementation(libs.guava.jdk8)
    implementation(libs.netty)
    implementation(libs.zookeeper)
    implementation(libs.commons.lang3)
    implementation("org.apache.commons:commons-text:1.9")
    implementation(libs.commons.collections4)
    implementation(libs.libthrift)
    implementation(libs.spring.core) {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation(libs.spring.web)
    implementation(libs.spring.webmvc)
    implementation(libs.spring.websocket)
    implementation(libs.spring.jdbc)
    implementation(libs.spring.context)
    implementation(libs.spring.context.support)
    implementation("org.springframework:spring-messaging:${Versions.spring}")
    implementation("org.springframework.security:spring-security-web:5.5.3")
    implementation("org.springframework.security:spring-security-config:5.5.3")
    implementation("org.springframework.security:spring-security-messaging:5.5.3")
    implementation(libs.spring.boot.starter.web)
    implementation("org.springframework.boot:spring-boot-starter-log4j2:${Versions.springBoot}")
    implementation(libs.hikariCP)
    implementation(libs.mybatis)
    implementation(libs.mybatis.spring)
    implementation(libs.mysql.connector.java)
    implementation(libs.caffeine)
    implementation(libs.jackson.core)
    implementation(libs.jackson.annotations)
    implementation(libs.jackson.databind)
    implementation(libs.log4j.api)
    implementation(libs.commons.codec)
    implementation(libs.jakarta.mail)
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation(libs.aspectjweaver)
    implementation(libs.curator.framework) {
        exclude(group = "org.apache.zookeeper", module = "zookeeper")
        exclude(group = "org.apache.curator", module = "curator-test")
    }
    runtimeOnly(libs.commons.lang)
    runtimeOnly(libs.slf4j.api)
    runtimeOnly(libs.log4j.jcl)
    runtimeOnly(libs.log4j.slf4j.impl)
    runtimeOnly(libs.log4j.core)
    testImplementation(libs.spring.test)
    testImplementation(libs.json.path)
    testImplementation(project(":pinpoint-profiler"))
    testImplementation(project(":pinpoint-collector"))
    testImplementation(libs.awaitility)
    testImplementation(libs.curator.test)
    testImplementation(project(":pinpoint-rpc"))
    testImplementation("org.skyscreamer:jsonassert:1.5.0")
    compileOnly(libs.javax.servlet.api)
    compileOnly("org.springframework.boot:spring-boot-starter-tomcat:${Versions.springBoot}")

    implementation(libs.hbase.shaded.client) {
        exclude(group = "org.slf4j", module = "slf4j-log4j12")
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation(libs.hbasewd) {
        exclude(group = "log4j", module = "log4j")
    }
}

description = "pinpoint-web"

//frontend {
//    nodeVersion.set("14.18.1")
//    assembleScript.set("run build:real")
//    cleanScript.set("run clean")
//    checkScript.set("run check")
//}