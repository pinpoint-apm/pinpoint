plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-bootstrap"))
    implementation(libs.maven.resolver.api)
    implementation("org.apache.maven.resolver:maven-resolver-spi:1.6.1")
    implementation("org.apache.maven.resolver:maven-resolver-util:1.6.1")
    implementation("org.apache.maven.resolver:maven-resolver-impl:1.6.1")
    implementation("org.apache.maven.resolver:maven-resolver-connector-basic:1.6.1")
    implementation("org.apache.maven.resolver:maven-resolver-transport-classpath:1.6.1")
    implementation("org.apache.maven.resolver:maven-resolver-transport-file:1.6.1")
    implementation("org.apache.maven.resolver:maven-resolver-transport-http:1.6.1")
    implementation("org.apache.maven.resolver:maven-resolver-transport-wagon:1.6.1")
    implementation("org.apache.maven:maven-resolver-provider:3.6.3")
    implementation(libs.commons.lang3)
    implementation(libs.log4j.api)
    implementation(libs.tinylog.api)
    implementation(libs.tinylog.impl) {
        exclude(group = "org.tinylog", module = "tinylog-api")
    }
    implementation(libs.tinylog.slf4j) {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "org.tinylog", module = "tinylog-api")
    }
    implementation(libs.junit)
    runtimeOnly(libs.slf4j.api)
    testImplementation(libs.log4j.slf4j.impl) {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
        exclude(group = "org.apache.logging.log4j", module = "log4j-core")
    }
    testImplementation(libs.log4j.core) {
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    }
    testImplementation(libs.log4j.jcl) {
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    }
}

description = "pinpoint-test"

tasks {
    processResources {
        expand("version" to version)
    }
}