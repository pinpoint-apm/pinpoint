plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    testCompileOnly(project(":pinpoint-profiler"))
    testCompileOnly(project(":pinpoint-profiler-test"))
    testCompileOnly(project(":pinpoint-test"))
    testImplementation("org.apache.tomcat:coyote:6.0.43")
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly("org.apache.tomcat:servlet-api:6.0.35")
    compileOnly("org.apache.tomcat:catalina:6.0.43")
}

description = "pinpoint-jsp-plugin"
