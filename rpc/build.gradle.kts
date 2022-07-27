plugins {
    `java-test-fixtures`
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(project(":pinpoint-annotations"))
    implementation(project(":pinpoint-commons"))
    implementation(project(":pinpoint-commons-profiler"))
    implementation(libs.netty)
    implementation(libs.log4j.slf4j.impl.jdk7)
    implementation(libs.log4j.core.jdk7)
    runtimeOnly(libs.slf4j.api)
    testFixturesApi(project(":pinpoint-testcase"))
    testFixturesImplementation(libs.netty)
    testFixturesImplementation(project(":pinpoint-commons"))
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
