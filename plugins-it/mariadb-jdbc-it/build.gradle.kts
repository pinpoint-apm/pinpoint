plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("org.mariadb.jdbc:mariadb-java-client:1.3.4")
    testImplementation("org.testcontainers:testcontainers:1.16.2")
    testImplementation("org.testcontainers:mariadb:1.16.2")
    testImplementation(project(":pinpoint-plugin-it-jdbc-test"))
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
    testImplementation(libs.log4j.api)
}

description = "pinpoint-mariadb-jdbc-driver-plugin-it"
