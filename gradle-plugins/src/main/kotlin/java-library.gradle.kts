package com.navercorp.pinpoint.gradle.plugins

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
    testImplementation("junit:junit")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.mockito:mockito-core:2.28.2")
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

dependencyManagement {
    dependencies {
//      <!-- serving performance metrics -->
        dependency("io.dropwizard.metrics:metrics-core:${Versions.metrics}")
        dependency("io.dropwizard.metrics:metrics-jvm:${Versions.metrics}")
        dependency("io.dropwizard.metrics:metrics-servlets:${Versions.metrics}") {
            exclude("com.papertrail:profiler")
        }

        dependency("com.jayway.jsonpath:json-path:1.2.0")
        dependency("com.fasterxml.jackson.core:jackson-core:${Versions.fastxmlJackson}")
        dependency("com.fasterxml.jackson.core:jackson-annotations:${Versions.fastxmlJackson}")
        dependency("com.fasterxml.jackson.core:jackson-databind:${Versions.fastxmlJackson}")
        dependency("org.codehaus.jackson:jackson-core-asl:${Versions.codehausJackson}")
        dependency("org.codehaus.jackson:jackson-mapper-asl:${Versions.codehausJackson}")

//        <!-- Logging dependencies -->
        dependency("log4j:log4j:${Versions.log4jv1}")

        dependency("com.zaxxer:HikariCP:4.0.3")
        dependency("org.mybatis:mybatis:3.5.7")
        dependency("org.mybatis:mybatis-spring:2.0.6") {
            exclude("org.springframework:spring-core")
            exclude("org.springframework:spring-tx")
            exclude("org.springframework:spring-jdbc")
            exclude("org.springframework:spring-context")
        }
        dependency("mysql:mysql-connector-java:8.0.27")

        dependency("io.netty:netty:${Versions.netty3}")
        dependency("io.netty:netty-all:${Versions.netty4}")

        dependency("com.github.ben-manes.caffeine:caffeine:2.9.2")

        dependency("org.apache.httpcomponents:httpclient:4.5.13")
        dependency("org.apache.httpcomponents:httpcore:4.4.14")

//      <!-- 2.7 (requires Java 8 -->
//      <!-- 2.6 (requires Java 7 -->
        dependency("commons-io:commons-io:2.6")
//      <!-- https://commons.apache.org/proper/commons-lang/changes-report.html -->
//      <!-- Lang 3.9 and onwards now targets Java 8, making use of features that arrived with Java 8.-->
        dependency("org.apache.commons:commons-lang3:3.8.1")
        dependency("commons-lang:commons-lang:2.6")
        dependency("org.apache.commons:commons-collections4:4.4")
//      <!-- Codec 1.14 (mirrors) requires Java 7-->
        dependency("commons-codec:commons-codec:1.14")
        dependency("com.sun.mail:jakarta.mail:1.6.7")

        dependency("org.ow2.asm:asm:${Versions.asm}")
        dependency("org.ow2.asm:asm-commons:${Versions.asm}")
        dependency("org.ow2.asm:asm-util:${Versions.asm}")
        dependency("org.ow2.asm:asm-tree:${Versions.asm}")
        dependency("org.ow2.asm:asm-analysis:${Versions.asm}")

        dependency("javax.servlet:javax.servlet-api:3.0.1")

        //TODO: https://github.com/spring-gradle-plugins/dependency-management-plugin/issues/320 can't build precompiled script with testImplementation.
        dependency("junit:junit:${Versions.junit}") {
            exclude("org.hamcrest:hamcrest-core")
        }
        dependency("org.awaitility:awaitility:3.1.5")

        dependency("org.apache.zookeeper:zookeeper:${Versions.zookeeper}") {
            exclude("org.jboss.netty:netty")
            exclude("org.slf4j:slf4j-log4j12")
            exclude("log4j:log4j")
        }
        dependency("org.apache.curator:curator-test:2.13.0") {
            exclude("org.apache.zookeeper:zookeeper")
        }

        dependency("org.apache.thrift:libthrift:0.12.0") {
            exclude("org.apache.httpcomponents:httpclient")
            exclude("org.apache.httpcomponents:httpcore")
            exclude("org.slf4j:slf4j-api")
        }

        dependency("com.google.guava:guava:30.1-android") {
            exclude("com.google.guava:listenablefuture")
        }
        dependency("com.google.inject:guice:4.2.2")
    }

// TODO: https://github.com/spring-gradle-plugins/dependency-management-plugin/issues/320 can't build precompiled script with testImplementation.
//    testImplementation {
//        dependencies {
//            dependency("org.springframework:spring-test:${Versions.spring}")
//        }
//    }
//    <!-- Logging dependencies -->
//
//    <dependency>
//    <groupId>org.apache.logging.log4j</groupId>
//    <artifactId>log4j-api</artifactId>
//    <version>${log4j2.version}</version>
//    <scope>compile</scope>
//    </dependency>
//    <dependency>
//    <groupId>org.apache.logging.log4j</groupId>
//    <artifactId>log4j-core</artifactId>
//    <version>${log4j2.version}</version>
//    <scope>runtime</scope>
//    <optional>true</optional>
//    </dependency>
//    <dependency>
//    <groupId>org.slf4j</groupId>
//    <artifactId>slf4j-api</artifactId>
//    <version>${slf4j.version}</version>
//    <scope>runtime</scope>
//    </dependency>
//    <dependency>
//    <groupId>org.apache.logging.log4j</groupId>
//    <artifactId>log4j-slf4j-impl</artifactId>
//    <version>${log4j2.version}</version>
//    <scope>runtime</scope>
//    <optional>true</optional>
//    </dependency>
//    <!-- thrift logging lib -->
//    <dependency>
//    <groupId>commons-logging</groupId>
//    <artifactId>commons-logging</artifactId>
//    <version>1.2</version>
//    <scope>runtime</scope>
//    <optional>true</optional>
//    </dependency>
//    <dependency>
//    <groupId>org.apache.logging.log4j</groupId>
//    <artifactId>log4j-jcl</artifactId>
//    <version>${log4j2.version}</version>
//    <scope>runtime</scope>
//    <optional>true</optional>
//    </dependency>
//    <dependency>
//    <groupId>org.apache.logging.log4j</groupId>
//    <artifactId>log4j-jul</artifactId>
//    <version>${log4j2.version}</version>
//    <scope>runtime</scope>
//    <optional>true</optional>
//    </dependency>
//
//    <dependency>
//    <groupId>log4j</groupId>
//    <artifactId>log4j</artifactId>
//    <version>${log4j1.version}</version>
//    </dependency>
}