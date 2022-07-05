plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java11")
}

dependencies {
    api(project(":pinpoint-commons-server"))
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-thrift"))
    api(project(":pinpoint-web")) // all transitive depdency off on pinpoint-web
    api(project(":pinpoint-rpc"))
    api(platform(project(":pinpoint-plugins")))
    api(platform(project(":pinpoint-agent-plugins")))

    implementation(libs.libthrift) {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "org.apache.httpcomponents", module = "httpcore")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "javax.annotation", module = "javax.annotation-api")
    }
    implementation(libs.spring.boot.starter.web) {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    implementation(libs.spring.boot.starter.log4j2)
    implementation(libs.spring.web)
    implementation(libs.spring.context.support)
    implementation(libs.spring.jdbc)
    implementation(libs.jakarta.mail)
    implementation("org.springframework.batch:spring-batch-core:4.3.3") {
        exclude(group = "javax.annotation", module = "javax.annotation-api")
    }
    implementation(libs.jakarta.annotation.api)
    implementation(libs.hikariCP)
    implementation(libs.mybatis)
    implementation(libs.mybatis.spring)
    implementation(libs.mysql.connector.java)
    runtimeOnly("com.sun.activation:jakarta.activation:1.2.2")
    runtimeOnly(libs.slf4j.api)
    runtimeOnly(libs.log4j.jcl) {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    runtimeOnly(libs.log4j.slf4j.impl)
    runtimeOnly(libs.log4j.core)
    testImplementation(libs.spring.test)
    testImplementation("org.springframework.batch:spring-batch-test:4.3.3")
    testImplementation(libs.commons.lang3)
    compileOnly(libs.spring.boot.starter.tomcat)
}

description = "pinpoint-batch"
