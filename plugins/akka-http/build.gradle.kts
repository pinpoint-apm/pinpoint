plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly("com.typesafe.akka:akka-http-core_2.12:10.1.0-RC1")
    testCompileOnly("com.typesafe.akka:akka-http-core_2.12:10.1.0-RC1")
    compileOnly("com.typesafe.akka:akka-http_2.12:10.1.0-RC1")
    testCompileOnly("com.typesafe.akka:akka-http_2.12:10.1.0-RC1")
    compileOnly(project(":pinpoint-bootstrap-core"))
    testCompileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    testCompileOnly(project(":pinpoint-commons"))
}

description = "pinpoint-akka-http-plugin"
