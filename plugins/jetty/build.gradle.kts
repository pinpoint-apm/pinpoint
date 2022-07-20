plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    implementation(project(":pinpoint-common-servlet"))
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("org.eclipse.jetty:jetty-server:9.2.11.v20150529")
    compileOnly("org.eclipse.jetty:jetty-servlet:9.2.11.v20150529")
}

description = "pinpoint-jetty-plugin"

sourceSets {
    main {
        java {
            srcDir("src/main/java-jetty")
        }
    }
}
