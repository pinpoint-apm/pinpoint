plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-annotations"))
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-commons-profiler"))
    implementation(libs.netty)
    implementation(libs.log4j.slf4j.impl.jdk7)
    implementation(libs.log4j.core.jdk7)
    runtimeOnly(libs.slf4j.api)
    testImplementation(project(":pinpoint-testcase"))
    testImplementation(libs.awaitility)
}

description = "pinpoint-rpc"

val testsJar by tasks.registering(Jar::class) {
    archiveClassifier.set("tests")
    from(sourceSets["test"].output)
}

(publishing.publications["pinpoint"] as MavenPublication).artifact(testsJar)
