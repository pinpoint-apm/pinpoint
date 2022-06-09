plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("org.mariadb.jdbc:mariadb-java-client:1.3.4")
    testImplementation("org.testcontainers:testcontainers:1.16.2")
    testImplementation("org.testcontainers:mariadb:1.16.2")
    testImplementation(project(":pinpoint-plugin-it-jdbc-test"))
}

description = "pinpoint-mariadb-jdbc-driver-plugin-it"
