plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    testImplementation(project(":pinpoint-profiler"))
    testImplementation(project(":pinpoint-profiler-test"))
    testImplementation(project(":pinpoint-test"))
    testImplementation("org.apache.tomcat:coyote:6.0.43")
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("org.apache.tomcat:servlet-api:6.0.35")
    compileOnly("org.apache.tomcat:catalina:6.0.43")
}

description = "pinpoint-jsp-plugin"
