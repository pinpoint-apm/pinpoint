plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    api(project(":pinpoint-common-servlet"))
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(libs.javax.servlet.api.v3)
}

description = "pinpoint-websphere-plugin"

sourceSets {
    main {
        java {
            srcDir("src/main/java-ibm")
        }
    }
}