plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
}

description = "pinpoint-thread-plugin-it"
