plugins {
    `java-library`
    `maven-publish`
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

    maven {
        url = uri("file:///Users/feelform/workspace/pinpoint/pinpoint/plugins/openwhisk/libs")
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

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(7))
    }
}
