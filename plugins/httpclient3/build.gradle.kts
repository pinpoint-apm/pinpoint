plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly(libs.httpcore)
    compileOnly("org.apache.httpcomponents:httpasyncclient:4.1.4")
    compileOnly("org.apache.httpcomponents:httpcore-nio:4.4.14")
    compileOnly(libs.httpclient)
    testCompileOnly(libs.httpclient)
    compileOnly("commons-httpclient:commons-httpclient:3.1")
}

description = "pinpoint-httpclient3-plugin"
