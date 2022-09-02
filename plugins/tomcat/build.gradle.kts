plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(project(":pinpoint-common-servlet"))
    implementation(project(":pinpoint-jboss-plugin"))
    testImplementation(project(":pinpoint-profiler"))
    testImplementation(project(":pinpoint-profiler-test"))
    testImplementation(project(":pinpoint-test"))
    testImplementation("org.apache.tomcat:coyote:6.0.43")
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly(libs.javax.servlet.api.v3)
    compileOnly("org.apache.tomcat:servlet-api:6.0.35")
    compileOnly("org.apache.tomcat:catalina:6.0.43")
    testCompileOnly("org.apache.tomcat:catalina:6.0.43")
}

description = "pinpoint-tomcat-plugin"
