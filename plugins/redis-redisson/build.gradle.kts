plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly("org.redisson:redisson:3.10.4")
}

description = "pinpoint-redis-redisson-plugin"
