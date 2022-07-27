plugins {
    id("com.navercorp.pinpoint.java8-library")
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
    implementation(project(":pinpoint-common-servlet"))
    testCompileOnly(project(":pinpoint-profiler"))
    testCompileOnly(project(":pinpoint-profiler-logging"))
    testCompileOnly(project(":pinpoint-profiler-test"))
    testCompileOnly(project(":pinpoint-test"))
    testImplementation(libs.commons.lang3)
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly("org.jboss.as:jboss-as-security:7.2.0.Final")
    testImplementation(libs.log4j.api)
    testCompileOnly(libs.javax.servlet.api.v3)
}

description = "pinpoint-jboss-plugin"
