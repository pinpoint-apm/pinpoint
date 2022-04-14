/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("pinpoint.java11-toolchain")
    id("com.intershop.gradle.jaxb") version Versions.jaxbPlugin
}

dependencies {
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:${Versions.jaxbImpl}")
    runtimeOnly("com.sun.xml.bind:jaxb-impl:${Versions.jaxbImpl}")
}

description = "pinpoint-hbase-schema-definition"

jaxb {
    // generate java code from schema
    javaGen {
        //generates a 'project' schema file from existing java code
        register("name") {
            packageName = "com.navercorp.pinpoint.hbase.schema.definition.xml"
            schema = file("../hbase-schema/src/main/java/com/navercorp/pinpoint/hbase/schema/reader/xml/pinpoint-hbase-1.0.xsd")
        }
    }
}