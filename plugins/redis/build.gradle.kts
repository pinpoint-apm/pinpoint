plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    testImplementation(project(":pinpoint-profiler"))
    testImplementation(project(":pinpoint-profiler-test"))
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly("redis.clients:jedis:2.4.2")
}

description = "pinpoint-redis-plugin"
