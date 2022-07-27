plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(project(":pinpoint-agent-proxy-common"))
    compileOnly(project(":pinpoint-profiler"))
    compileOnly(project(":pinpoint-commons"))
    testCompileOnly(project(":pinpoint-profiler"))
    compileOnly(project(":pinpoint-bootstrap-core"))
    testImplementation(libs.log4j.api)
}

description = "pinpoint-agent-proxy-user-plugin"
