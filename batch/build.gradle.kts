plugins {
    id("com.navercorp.pinpoint.gradle.plugins.toolchain.java11")
    id("com.navercorp.pinpoint.gradle.plugins.bom.plugins-assembly")
    id("com.navercorp.pinpoint.gradle.plugins.bom.agent-plugins")
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
    implementation(libs.spring.web)
    implementation(libs.spring.context.support)
    implementation(libs.spring.jdbc)
    implementation("com.sun.mail:jakarta.mail")
    implementation("org.springframework.batch:spring-batch-core:4.3.3")
    implementation("com.zaxxer:HikariCP")
    implementation("org.mybatis:mybatis")
    implementation("org.mybatis:mybatis-spring")
    implementation("mysql:mysql-connector-java")
    runtimeOnly("com.sun.activation:jakarta.activation:1.2.2")
    runtimeOnly(libs.slf4j.api)
    runtimeOnly("org.apache.logging.log4j:log4j-jcl:${Versions.log4jJDK8}") {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:${Versions.log4jJDK8}")
    runtimeOnly(libs.log4j.core)
    testImplementation(libs.spring.test)
    testImplementation("org.springframework.batch:spring-batch-test:4.3.3")
    testImplementation("org.apache.commons:commons-lang3")
    compileOnly("org.springframework.boot:spring-boot-starter-tomcat:${Versions.springBoot}")
}

description = "pinpoint-batch"
