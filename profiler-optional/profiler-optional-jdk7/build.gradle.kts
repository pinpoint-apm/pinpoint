plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-profiler"))
    implementation(libs.log4j.api.jdk7)
    testImplementation(libs.asm.core)
    testImplementation(libs.asm.commons)
    testImplementation(libs.asm.util)
    testImplementation(libs.asm.tree)
    testImplementation(libs.log4j.slf4j.impl.jdk7)
    testImplementation(libs.log4j.core.jdk7)
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