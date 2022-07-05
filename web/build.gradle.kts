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
    implementation(libs.libthrift) {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "org.apache.httpcomponents", module = "httpcore")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "javax.annotation", module = "javax.annotation-api")
    }
    implementation(libs.spring.core) {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation(libs.spring.web)
    implementation(libs.spring.webmvc)
    implementation(libs.spring.websocket)
    implementation(libs.spring.jdbc)
    implementation(libs.spring.context)
    implementation(libs.spring.context.support)
    implementation(libs.spring.messaging)
    implementation(libs.spring.security.web)
    implementation(libs.spring.security.config)
    implementation(libs.spring.security.messaging)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.log4j2)
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
    implementation(libs.jakarta.bind.api)
    implementation(libs.aspectjweaver)
    implementation(libs.curator.client) {
        exclude(group = "org.apache.zookeeper", module = "zookeeper")
        exclude(group = "org.apache.curator", module = "curator-test")
    }
    implementation(libs.curator.framework) {
        exclude(group = "org.apache.zookeeper", module = "zookeeper")
        exclude(group = "org.apache.curator", module = "curator-test")
        exclude(group = "org.apache.curator", module = "curator-client")
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
    compileOnly(libs.spring.boot.starter.tomcat)

    implementation(libs.hbase.shaded.client) {
        exclude(group = "org.slf4j", module = "slf4j-log4j12")
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation(libs.hbasewd) {
        exclude(group = "log4j", module = "log4j")
    }
    implementation(libs.commons.math3)
}

description = "pinpoint-web"

//frontend {
//    nodeVersion.set("14.18.1")
//    assembleScript.set("run build:real")
//    cleanScript.set("run clean")
//    checkScript.set("run check")
//}