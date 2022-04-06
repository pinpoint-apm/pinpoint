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
        dependency("org.springframework:spring-webmvc:${Versions.spring}")
        dependency("org.springframework:spring-websocket:${Versions.spring}")
        dependency("org.springframework:spring-jdbc:${Versions.spring}")
        dependency("org.springframework:spring-tx:${Versions.spring}")
        dependency("org.springframework:spring-context-support:${Versions.spring}")

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