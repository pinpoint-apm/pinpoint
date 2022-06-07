plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java11")
}

dependencies {
    implementation(libs.zookeeper)
    implementation(libs.spring.context)
    implementation("org.springframework.boot:spring-boot:${Versions.springBoot}")
    implementation(libs.log4j.api.jdk7)
    implementation(libs.curator.framework) {
        exclude(group = "org.apache.zookeeper", module = "zookeeper")
        exclude(group = "org.apache.curator", module = "curator-test")
    }
    runtimeOnly(libs.slf4j.api)
    runtimeOnly(libs.log4j.slf4j.impl)
    runtimeOnly(libs.log4j.core)
    runtimeOnly(libs.log4j.jcl)
    testImplementation(libs.spring.test)
    testImplementation(libs.curator.test)
    testImplementation(project(":pinpoint-testcase"))
    testImplementation(libs.awaitility)
}

description = "pinpoint-commons-server-cluster"
