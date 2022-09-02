plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    implementation(project(":pinpoint-common-servlet"))
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
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
