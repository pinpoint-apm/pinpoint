plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-plugin-it-utils"))
    testImplementation("com.alibaba:dubbo:2.5.3")
}

description = "pinpoint-dubbo-plugin-it"
