plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("com.bbossgroups.plugins:bboss-elasticsearch-rest-jdbc:5.6.9")
    testImplementation("org.elasticsearch.client:elasticsearch-rest-high-level-client:7.3.0")
    testImplementation("pl.allegro.tech:embedded-elasticsearch:2.10.0")
    testImplementation(project(":pinpoint-bootstrap-core"))
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
}

description = "pinpoint-elasticsearch-bboss-plugin-it"
