plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(project(":pinpoint-common-servlet"))
    compileOnly(project(":pinpoint-bootstrap-core"))
    testCompileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly(libs.javax.servlet.api.v3)
}

description = "pinpoint-weblogic-plugin"

sourceSets {
    main {
        java {
            srcDir("src/main/java-oracle")
        }
    }
}