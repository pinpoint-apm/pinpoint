plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java8")
}

dependencies {
    compileOnly(project(":pinpoint-bootstrap-core"))
    compileOnly(project(":pinpoint-commons"))
    compileOnly("io.lettuce:lettuce-core:5.1.2.RELEASE")
}

description = "pinpoint-redis-lettuce-plugin"
