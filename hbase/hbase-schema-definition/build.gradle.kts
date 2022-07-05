plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java11")
    alias(libs.plugins.jaxb)
}

dependencies {
    implementation(libs.jakarta.annotation.api)
    implementation(libs.jakarta.bind.api)
    runtimeOnly(libs.jakarta.jaxb.impl)
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