plugins {
    id("io.spring.dependency-management")
}

dependencyManagement {
    dependencies {
        dependency("org.springframework:spring-core:${Versions.spring}") {
            exclude("commons-logging:commons-logging")
        }
        dependency("org.springframework:spring-beans:${Versions.spring}")
        dependency("org.springframework:spring-context:${Versions.spring}")
        dependency("org.springframework:spring-orm:${Versions.spring}")
        dependency("org.springframework:spring-web:${Versions.spring}")
        dependency("org.springframework:spring-webmvc:${Versions.spring}")
        dependency("org.springframework:spring-websocket:${Versions.spring}")
        dependency("org.springframework:spring-jdbc:${Versions.spring}")
        dependency("org.springframework:spring-tx:${Versions.spring}")
        dependency("org.springframework:spring-context-support:${Versions.spring}")
    }
}