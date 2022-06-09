plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("com.zaxxer:HikariCP-java6:2.3.13")
}

description = "pinpoint-hikaricp-plugin-jdk7-it"
