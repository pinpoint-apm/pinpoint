plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-profiler"))
    implementation(libs.log4j.api.jdk7)
    implementation(libs.bundles.asm)
    testImplementation(project(":pinpoint-test"))
    testImplementation(libs.spring.test)
    testImplementation(libs.spring.context)
    testImplementation(libs.log4j.slf4j.impl.jdk7)
    testImplementation(libs.log4j.core.jdk7)
}

description = "pinpoint-profiler-optional-jdk8"

sourceSets {
    main {
        java {
            srcDir("src/main/java-ibm")
        }
    }
}