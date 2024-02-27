plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-commons"))
    compileOnly(project(":pinpoint-profiler"))
    testCompileOnly(project(":pinpoint-profiler"))
    implementation(libs.log4j.api)
    implementation(libs.bundles.asm)
    testImplementation(project(":pinpoint-test"))
    testImplementation(libs.spring.test)
    testImplementation(libs.spring.context)
    testImplementation(libs.log4j.slf4j.impl)
    testImplementation(libs.log4j.core)
}

description = "pinpoint-profiler-optional-jdk8"

sourceSets {
    main {
        java {
            srcDir("src/main/java-ibm")
        }
    }
}