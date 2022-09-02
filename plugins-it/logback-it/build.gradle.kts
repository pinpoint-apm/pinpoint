plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
    testImplementation(libs.log4j.api)
}

description = "pinpoint-logback-plugin-it"
