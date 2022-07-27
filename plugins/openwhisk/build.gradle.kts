plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(files("libs/internal/com/apache/openwhisk/openwhisk-common/1.0.0/openwhisk-common-1.0.0.jar"))
    compileOnly("com.typesafe.akka:akka-http-core_2.12:10.1.0-RC1")
    compileOnly("com.typesafe.akka:akka-http_2.12:10.1.0-RC1")
    compileOnly("org.scala-lang:scala-library:2.11.11")
    compileOnly("io.spray:spray-json_2.12:1.3.4")
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
}

description = "pinpoint-openwhisk-plugin"
