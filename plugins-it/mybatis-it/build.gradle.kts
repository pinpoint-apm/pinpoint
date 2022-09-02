plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(libs.spring4.beans)
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation(libs.mybatis)
    testImplementation(libs.mybatis.spring)
    testImplementation(project(":pinpoint-test")) {
        exclude(group = "org.tinylog", module = "slf4j-tinylog")
    }
}

description = "pinpoint-mybatis-plugin-it"
