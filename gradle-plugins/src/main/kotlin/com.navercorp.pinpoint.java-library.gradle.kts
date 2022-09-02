plugins {
    `java-library`
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
        mavenContent {
            releasesOnly()
        }
    }
    maven {
        url = uri("file://${rootDir}/libs")
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
        implementation("org.apache.curator:curator-client:4.2.0")
        implementation("org.apache.curator:curator-framework:4.2.1")
        testImplementation("org.apache.curator:curator-test:2.13.0")
        testImplementation("org.apache.zookeeper:zookeeper:3.4.14")

        // libthrift exclusions
        implementation("org.apache.httpcomponents:httpclient:4.5.13")
        implementation("org.apache.httpcomponents:httpcore:4.4.14")
        implementation("org.slf4j:slf4j-api:1.7.30")
    }
}

group = "com.navercorp.pinpoint"
version = System.getProperty("version")

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}