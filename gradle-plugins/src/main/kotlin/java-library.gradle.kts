package com.navercorp.pinpoint.gradle.plugins

plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
        mavenContent {
            releasesOnly()
        }
    }
}

dependencies {
    testImplementation("junit:junit") {
        exclude(group = "org.hamcrest", module = "hamcrest-core")
        exclude(group = "org.hamcrest", module = "hamcrest-library")
    }
    testImplementation("org.hamcrest:hamcrest")
    testImplementation("org.mockito:mockito-core")
    constraints {
        testImplementation("junit:junit:4.13.2")
        testImplementation("org.hamcrest:hamcrest:2.2")
        testImplementation("org.mockito:mockito-core:2.28.2")

        // zookeeper exclusion
        implementation("io.netty:netty:3.10.6.Final")
        implementation("org.slf4j:slf4j-log4j12:1.7.30")
        implementation("log4j:log4j:1.2.17")

        // curator-framework exclusion
        testImplementation("org.apache.curator:curator-test:2.13.0")
        testImplementation("org.apache.zookeeper:zookeeper:3.4.14")

        // libthrift exclusions
        implementation("org.apache.httpcomponents:httpclient:4.5.13")
        implementation("org.apache.httpcomponents:httpcore:4.4.14")
        implementation("org.slf4j:slf4j-api:1.7.30")
    }
}

group = "com.navercorp.pinpoint"

publishing {
    publications.create<MavenPublication>("pinpoint") {
        from(components["java"])
        pom {
            name.set("pinpoint")
            description.set("Pinpoint APM, Application Performance Management tool for large-scale distributed systems")
            url.set("https://github.com/pinpoint-apm/pinpoint")
            licenses {
                license {
                    name.set("Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }
            inceptionYear.set("2012")

            scm {
                url.set("https://github.com/pinpoint-apm/pinpoint")
                connection.set("scm:git:git://github.com/pinpoint-apm/pinpoint.git")
                developerConnection.set("scm:git:ssh://git@github.com/pinpoint-apm/pinpoint.git")
            }
            developers {
                developer {
                    id.set("emeroad")
                    name.set("WoonDuk Kang")
                    email.set("emeroad@gamil.com")
                    organization.set("NAVER Corp.")
                    organizationUrl.set("http://www.naver.com")
                }
            }
        }
    }
    repositories {
        maven {
            name = "ossrh"

            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
        }
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}