plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("org.elasticsearch.client:elasticsearch-rest-high-level-client:7.3.0")
    testImplementation("pl.allegro.tech:embedded-elasticsearch:2.10.0")
    testImplementation(project(":pinpoint-elasticsearch-plugin"))
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
}

description = "pinpoint-elasticsearch-plugin-it"
