plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("org.apache.ibatis:ibatis-sqlmap:2.3.4.726")
    testImplementation("org.springframework:spring-ibatis:2.0.8")
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
}

description = "pinpoint-ibatis-plugin-it"
