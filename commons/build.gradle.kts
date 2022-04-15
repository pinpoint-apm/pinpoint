/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java7")
}

val groovyScript by configurations.creating

dependencies {
    testImplementation("org.apache.logging.log4j:log4j-api:${Versions.log4jJDK7}")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:${Versions.log4jJDK7}")
    testImplementation("org.apache.logging.log4j:log4j-core:${Versions.log4jJDK7}")
    testImplementation("org.apache.commons:commons-lang3")
    groovyScript("org.codehaus.groovy:groovy-all:2.4.5")
}

description = "pinpoint-commons"

tasks.register<JavaExec>("runGroovyScript") {

}
