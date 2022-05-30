plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java11")
    id("com.navercorp.pinpoint.gradle.plugins.bom.plugins-assembly")
    id("com.navercorp.pinpoint.gradle.plugins.bom.grpc")
    id("com.navercorp.pinpoint.gradle.plugins.bom.curator")
}

dependencies {
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-commons-buffer"))
    api(project(":pinpoint-commons-profiler"))
    api(project(":pinpoint-commons-server"))
    api(project(":pinpoint-commons-server-cluster"))
    api(project(":pinpoint-commons-hbase"))
    api(project(":pinpoint-rpc"))
    api(project(":pinpoint-thrift"))
    api(project(":pinpoint-grpc"))
    implementation("org.apache.zookeeper:zookeeper")
    api(project(":pinpoint-profiler"))
    implementation("com.google.guava:guava:30.1-jre")
    implementation(libs.netty)
    implementation("org.apache.commons:commons-lang3")
    implementation("org.apache.commons:commons-collections4")
    implementation("org.apache.thrift:libthrift:0.15.0")
    implementation(libs.jackson.core)
    implementation(libs.jackson.annotations)
    implementation(libs.jackson.databind)
    implementation(libs.slf4j.api)
    implementation(libs.log4j.slf4j.impl)
    implementation(libs.spring.core) {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation(libs.spring.context)
    implementation(libs.spring.orm)
    implementation(libs.spring.web)
    implementation(libs.spring.webmvc)
    implementation("org.springframework.boot:spring-boot-starter-web:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-log4j2:${Versions.springBoot}")
    implementation(libs.metrics.core)
    implementation(libs.metrics.jvm)
    implementation(libs.metrics.servlets) {
        exclude(group = "com.papertrail", module = "profiler")
    }
    runtimeOnly("commons-lang:commons-lang")
    runtimeOnly(libs.log4j.jcl)
    runtimeOnly(libs.log4j.core)
    testImplementation(libs.spring.test)
    testImplementation("org.springframework.boot:spring-boot-test:${Versions.springBoot}")
    testImplementation("org.awaitility:awaitility")
    testImplementation(project(":pinpoint-rpc"))
    compileOnly("javax.servlet:javax.servlet-api:4.0.1")

    implementation(libs.hbase.shaded.client) {
        exclude("org.slf4j:slf4j-log4j12")
        exclude("commons-logging:commons-logging")
    }
    implementation(libs.hbasewd) {
        exclude("log4j:log4j")
    }
}

description = "pinpoint-collector"
