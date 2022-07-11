plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(libs.httpcore)
    compileOnlyApi("org.apache.httpcomponents:httpasyncclient:4.1.4")
    compileOnly("org.apache.httpcomponents:httpcore-nio:4.4.14")
    compileOnly(libs.httpclient)
    compileOnly("commons-httpclient:commons-httpclient:3.1")
}

description = "pinpoint-httpclient4-plugin"
