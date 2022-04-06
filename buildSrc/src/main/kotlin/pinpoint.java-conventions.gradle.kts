plugins {
    `java-library`
    `maven-publish`
    id("io.spring.dependency-management")
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }

    maven {
        url = uri("https://jcenter.bintray.com")
    }

    maven {
        url = uri("https://repository.jboss.org/nexus/content/repositories/thirdparty-releases/")
    }
}

dependencies {
    testImplementation("junit:junit:${Versions.junit}")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.mockito:mockito-core:2.28.2")
}

group = "com.navercorp.pinpoint"
version = "2.4.0-SNAPSHOT"

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
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
        dependency("com.jayway.jsonpath:json-path:1.2.0")
    }

// TODO: https://github.com/spring-gradle-plugins/dependency-management-plugin/issues/320 can't build precompiled script with testImplementation.
//    testImplementation {
//        dependencies {
//            dependency("org.springframework:spring-test:${Versions.spring}")
//        }
//    }
}