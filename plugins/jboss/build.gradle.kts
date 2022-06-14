plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

repositories {
    maven {
        url = uri("https://jcenter.bintray.com")
    }

    maven {
        url = uri("https://repository.jboss.org/nexus/content/repositories/thirdparty-releases/")
    }
}

dependencies {
    api(project(":pinpoint-common-servlet"))
    testImplementation(project(":pinpoint-profiler"))
    testImplementation(project(":pinpoint-profiler-logging"))
    testImplementation(project(":pinpoint-profiler-test"))
    testImplementation(project(":pinpoint-test"))
    testImplementation(libs.commons.lang3)
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("org.jboss.as:jboss-as-security:7.2.0.Final")
}

description = "pinpoint-jboss-plugin"
