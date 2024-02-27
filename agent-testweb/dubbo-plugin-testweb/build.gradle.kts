plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.logging)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.springdoc.openapi.ui)
    implementation("org.apache.dubbo:dubbo:2.7.18")
    implementation("org.apache.dubbo:dubbo-dependencies-zookeeper:2.7.18") {
        exclude(group = "org.apache.curator", module = "curator-framework")
    }
    compileOnly(libs.spring.boot.starter.tomcat)
}

description = "pinpoint-dubbo-plugin-testweb"
