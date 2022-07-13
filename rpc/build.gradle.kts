plugins {
    `java-test-fixtures`
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
    testFixturesApi(project(":pinpoint-testcase"))
    testFixturesImplementation(libs.netty)
    testFixturesApi(libs.awaitility)
    testFixturesImplementation(libs.log4j.api)
    testFixturesImplementation("junit:junit:4.13.2") {
        exclude(group = "org.hamcrest", module = "hamcrest-core")
        exclude(group = "org.hamcrest", module = "hamcrest-library")
    }
}

description = "pinpoint-rpc"

val testsJar by tasks.registering(Jar::class) {
    archiveClassifier.set("tests")
    from(sourceSets["test"].output)
}

(publishing.publications["pinpoint"] as MavenPublication).artifact(testsJar)
