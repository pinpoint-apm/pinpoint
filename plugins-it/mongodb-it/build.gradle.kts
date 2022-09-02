plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation(project(":pinpoint-mongodb-driver-plugin"))
    testImplementation("org.mongodb:bson:3.7.0")
    testImplementation("org.mongodb:mongodb-driver:3.9.0")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:3.4.6")
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
}

description = "pinpoint-mongodb-plugin-it"
