/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java11-toolchain")
    id("pinpoint.hbase-bom")
}

dependencies {
    api(project(":pinpoint-hbase-schema"))
    implementation("org.springframework.boot:spring-boot-configuration-processor:${Versions.springBoot}")
    implementation("org.apache.commons:commons-lang3")
    implementation("org.springframework.boot:spring-boot-starter:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-log4j2:${Versions.springBoot}")
    implementation("org.springframework:spring-tx")
    implementation("org.apache.logging.log4j:log4j-1.2-api:2.14.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test:${Versions.springBoot}")
}

description = "pinpoint-hbase-schema-manager"
