plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-profiler"))
    testCompileOnly(project(":pinpoint-profiler"))
    implementation(libs.log4j.api)
    testImplementation(libs.asm.core)
    testImplementation(libs.asm.commons)
    testImplementation(libs.asm.util)
    testImplementation(libs.asm.tree)
    testImplementation(libs.log4j.slf4j.impl)
    testImplementation(libs.log4j.core)
    testImplementation(libs.guice)
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