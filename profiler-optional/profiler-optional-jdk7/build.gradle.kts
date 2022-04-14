/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java7-toolchain")
    id("pinpoint.asm-bom")
}

dependencies {
    compileOnly(project(":pinpoint-profiler"))
    implementation("org.apache.logging.log4j:log4j-api:${Versions.log4jJDK7}")
    testImplementation("org.ow2.asm:asm")
    testImplementation("org.ow2.asm:asm-commons")
    testImplementation("org.ow2.asm:asm-util")
    testImplementation("org.ow2.asm:asm-tree")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:${Versions.log4jJDK7}")
    testImplementation("org.apache.logging.log4j:log4j-core:${Versions.log4jJDK7}")
}

description = "pinpoint-profiler-optional-jdk7"

sourceSets {
    main {
        java {
            srcDir("src/main/java-ibm")
            srcDir("src/main/java-oracle")
        }
    }
}