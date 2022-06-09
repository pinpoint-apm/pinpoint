plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation(libs.httpcore)
    testImplementation("org.apache.httpcomponents:httpasyncclient:4.1.4")
    testImplementation("org.apache.httpcomponents:httpcore-nio:4.4.14")
    testImplementation(libs.httpclient)
}

description = "pinpoint-httpclient4-plugin-it"
