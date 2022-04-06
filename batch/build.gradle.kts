plugins {
    id("pinpoint.java11-conventions")
    id("pinpoint.plugins-assembly-conventions")
    id("pinpoint.agent-plugins-conventions")
}

dependencies {
    api(project(":pinpoint-commons-server"))
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-thrift"))
    api(project(":pinpoint-web")) // all transitive depdency off on pinpoint-web
    api(project(":pinpoint-rpc"))
    implementation("org.apache.thrift:libthrift:0.15.0")
    implementation("org.springframework.boot:spring-boot-starter-web:${Versions.springBoot}") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    implementation("org.springframework.boot:spring-boot-starter-log4j2:${Versions.springBoot}")
    implementation("org.springframework:spring-web")
    implementation("org.springframework:spring-context-support")
    implementation("org.springframework:spring-jdbc")
    implementation("com.sun.mail:jakarta.mail:1.6.7")
    implementation("org.springframework.batch:spring-batch-core:4.3.3")
    implementation("com.zaxxer:HikariCP")
    implementation("org.mybatis:mybatis")
    implementation("org.mybatis:mybatis-spring")
    implementation("mysql:mysql-connector-java")
    runtimeOnly("com.sun.activation:jakarta.activation:1.2.2")
    runtimeOnly("org.slf4j:slf4j-api:${Versions.slf4j}")
    runtimeOnly("org.apache.logging.log4j:log4j-jcl:${Versions.log4jJDK8}") {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:${Versions.log4jJDK8}")
    runtimeOnly("org.apache.logging.log4j:log4j-core:${Versions.log4jJDK8}")
    testImplementation("org.springframework:spring-test:${Versions.spring}")
    testImplementation("org.springframework.batch:spring-batch-test:4.3.3")
    testImplementation("org.apache.commons:commons-lang3")
    compileOnly("org.springframework.boot:spring-boot-starter-tomcat:${Versions.springBoot}")
}

description = "pinpoint-batch"
