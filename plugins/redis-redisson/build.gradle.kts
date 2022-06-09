plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("org.redisson:redisson:3.10.4")
}

description = "pinpoint-redis-redisson-plugin"
