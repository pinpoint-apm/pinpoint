plugins {
    id("pinpoint.java11-conventions")
    id("pinpoint.plugins-assembly-conventions")
    id("pinpoint.agent-plugins-conventions")
}

dependencies {
    api(project(":pinpoint-commons-server"))
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-thrift"))
    api(project(":pinpoint-web"))
    api(project(":pinpoint-rpc"))
    implementation("org.apache.thrift:libthrift:0.15.0")
    implementation("org.springframework.boot:spring-boot-starter-web:2.5.7")
    implementation("org.springframework.boot:spring-boot-starter-log4j2:2.5.7")
    implementation("org.springframework:spring-web:5.3.13")
    implementation("org.springframework:spring-context-support:5.3.13")
    implementation("org.springframework:spring-jdbc:5.3.13")
    implementation("com.sun.mail:jakarta.mail:1.6.7")
    implementation("org.springframework.batch:spring-batch-core:4.3.3")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.mybatis:mybatis:3.5.7")
    implementation("org.mybatis:mybatis-spring:2.0.6")
    implementation("mysql:mysql-connector-java:8.0.27")
    runtimeOnly("com.sun.activation:jakarta.activation:1.2.2")
    runtimeOnly("org.slf4j:slf4j-api:1.7.30")
    runtimeOnly("org.apache.logging.log4j:log4j-jcl:2.17.1")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.17.1")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.17.1")
    testImplementation("org.springframework:spring-test:5.3.13")
    testImplementation("org.springframework.batch:spring-batch-test:4.3.3")
    compileOnly("org.springframework.boot:spring-boot-starter-tomcat:2.5.7")
}

description = "pinpoint-batch"
