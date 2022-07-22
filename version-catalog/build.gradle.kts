plugins {
    `version-catalog`
    `maven-publish`
}

catalog {
    // declare the aliases, bundles and versions in this block
    versionCatalog {
        from(rootProject.files("gradle/libs.versions.toml"))
//        version("spring", "5.3.20")
//        library("spring-core", "org.springframework", "spring-core").versionRef("spring")
//        library("spring-context", "org.springframework", "spring-context").versionRef("spring")
//        library("spring-orm", "org.springframework", "spring-orm").versionRef("spring")
//        library("spring-test", "org.springframework", "spring-test").versionRef("spring")
//        library("spring-web", "org.springframework", "spring-web").versionRef("spring")
//        library("spring-webmvc", "org.springframework", "spring-webmvc").versionRef("spring")
//        library("spring-websocket", "org.springframework", "spring-websocket").versionRef("spring")
//        library("spring-jdbc", "org.springframework", "spring-jdbc").versionRef("spring")
//        library("spring-tx", "org.springframework", "spring-tx").versionRef("spring")
//        library("spring-oxm", "org.springframework", "spring-oxm").versionRef("spring")
//        library("spring-instrument", "org.springframework", "spring-instrument").versionRef("spring")
//        library("spring-context-support", "org.springframework", "spring-context-support").versionRef("spring")
//        library("spring-aop", "org.springframework", "spring-aop").versionRef("spring")
//        library("spring-aspects", "org.springframework", "spring-aspects").versionRef("spring")
//        library("spring-webflux", "org.springframework", "spring-webflux").versionRef("spring")
//        library("spring-messaging", "org.springframework", "spring-messaging").versionRef("spring")
//
//        version("spring4", "4.3.30.RELEASE")
//        library("spring4-beans", "org.springframework", "spring-beans").versionRef("spring4")
//        library("spring4-context", "org.springframework", "spring-context").versionRef("spring4")
//        library("spring4-test", "org.springframework", "spring-test").versionRef("spring4")
//        library("spring4-web", "org.springframework", "spring-web").versionRef("spring4")
//        library("spring4-webmvc", "org.springframework", "spring-webmvc").versionRef("spring4")
//
//        version("springBoot", "2.5.12")
//        library("spring-boot", "org.springframework.boot", "spring-boot").versionRef("springBoot")
//        library("spring-boot-test", "org.springframework.boot", "spring-boot-test").versionRef("springBoot")
//        library("spring-boot-autoconfigure", "org.springframework.boot", "spring-boot-autoconfigure").versionRef("springBoot")
//        library("spring-boot-configuration-processor", "org.springframework.boot", "spring-boot-configuration-processor").versionRef("springBoot")
//        library("spring-boot-starter", "org.springframework.boot", "spring-boot-starter").versionRef("springBoot")
//        library("spring-boot-starter-test", "org.springframework.boot", "spring-boot-starter-test").versionRef("springBoot")
//        library("spring-boot-starter-web", "org.springframework.boot", "spring-boot-starter-web").versionRef("springBoot")
//        library("spring-boot-starter-logging", "org.springframework.boot", "spring-boot-starter-logging").versionRef("springBoot")
//        library("spring-boot-starter-actuator", "org.springframework.boot", "spring-boot-starter-actuator").versionRef("springBoot")
//        library("spring-boot-starter-tomcat", "org.springframework.boot", "spring-boot-starter-tomcat").versionRef("springBoot")
//        library("spring-boot-starter-log4j2", "org.springframework.boot", "spring-boot-starter-log4j2").versionRef("springBoot")
//
//        version("springSecurity", "5.5.3")
//        library("spring-security-core", "org.springframework.security", "spring-security-core").versionRef("springSecurity")
//        library("spring-security-web", "org.springframework.security", "spring-security-web").versionRef("springSecurity")
//        library("spring-security-config", "org.springframework.security", "spring-security-config").versionRef("springSecurity")
//        library("spring-security-messaging", "org.springframework.security", "spring-security-messaging").versionRef("springSecurity")


//        json-path = { module = "com.jayway.jsonpath:json-path", version = "1.2.0" }
//
//        # serving performance metrics
//        metrics-core = { module = "io.dropwizard.metrics:metrics-core", version.ref = "metrics" }
//        metrics-jvm = { module = "io.dropwizard.metrics:metrics-jvm", version.ref = "metrics" }
//        metrics-servlets = { module = "io.dropwizard.metrics:metrics-servlets", version.ref = "metrics" }
//
//        jackson-core = { module = "com.fasterxml.jackson.core:jackson-core", version.ref = "fastxml-jackson" }
//        jackson-annotations = { module = "com.fasterxml.jackson.core:jackson-annotations", version.ref = "fastxml-jackson" }
//        jackson-databind = { module = "com.fasterxml.jackson.core:jackson-databind", version.ref = "fastxml-jackson" }
//        jackson-dataformat-yaml = { module = "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml", version.ref = "fastxml-jackson" }
//        snakeyaml = { module = "org.yaml:snakeyaml", version = "1.27" }
//
//        hbase-shaded-client = { module = "org.apache.hbase:hbase-shaded-client", version = "1.7.1" }
//        hbase-client = { module = "org.apache.hbase:hbase-client", version = "2.4.11" }
//
//        springdoc-openapi-ui = { module = "org.springdoc:springdoc-openapi-ui", version = "1.4.4" }
//
//        jackson-core-asl = { module = "org.codehaus.jackson:jackson-core-asl", version.ref = "jacksonASL" }
//        jackson-mapper-asl = { module = "org.codehaus.jackson:jackson-mapper-asl", version.ref = "jacksonASL" }
//        hbasewd = { module = "com.sematext.hbasewd:hbasewd", version = "0.1.0" }
//
//        # Logging dependencies
//            log4j-api-jdk7 = { module = "org.apache.logging.log4j:log4j-api", version.ref = "log4j2-jdk7" }
//        log4j-api = { module = "org.apache.logging.log4j:log4j-api", version.ref = "log4j2" }
//        log4j-core-jdk7 = { module = "org.apache.logging.log4j:log4j-core", version.ref = "log4j2-jdk7" }
//        log4j-core = { module = "org.apache.logging.log4j:log4j-core", version.ref = "log4j2" }
//        slf4j-api = { module = "org.slf4j:slf4j-api", version = "1.7.30" }
//        log4j-slf4j-impl-jdk7 = { module = "org.apache.logging.log4j:log4j-slf4j-impl", version.ref = "log4j2-jdk7" }
//        log4j-slf4j-impl = { module = "org.apache.logging.log4j:log4j-slf4j-impl", version.ref = "log4j2" }
//
//        # thrift logging lib
//        commons-logging = { module = "commons-logging:commons-logging", version = "1.2" }
//        log4j-jcl-jdk7 = { module = "org.apache.logging.log4j:log4j-jcl", version.ref = "log4j2-jdk7" }
//        log4j-jcl = { module = "org.apache.logging.log4j:log4j-jcl", version.ref = "log4j2" }
//        log4j-jul = { module = "org.apache.logging.log4j:log4j-jul", version.ref = "log4j2" }
//        log4j = { module = "log4j:log4j", version = "1.2.17" }
//
//        hikariCP = { module = "com.zaxxer:HikariCP", version = "4.0.3" }
//        mybatis = { module = "org.mybatis:mybatis", version = "3.5.7" }
//        # 2.0.x java8 & spring5 https://mybatis.org/spring/index.html
//        mybatis-spring = { module = "org.mybatis:mybatis-spring", version = "2.0.6" }
//        mysql-connector-java = { module = "mysql:mysql-connector-java", version = "8.0.27" }
//
//        netty = { module = "io.netty:netty", version = "3.10.6.Final" }
//        netty-bom = { module = "io.netty:netty-bom", version.ref = "netty4" }
//        netty-all = { module = "io.netty:netty-all", version.ref = "netty4" }
//        netty-buffer = { module = "io.netty:netty-buffer", version.ref = "netty4" }
//        netty-codec = { module = "io.netty:netty-codec", version.ref = "netty4" }
//        netty-codec-dns = { module = "io.netty:netty-codec-dns", version.ref = "netty4" }
//        netty-codec-http = { module = "io.netty:netty-codec-http", version.ref = "netty4" }
//        netty-codec-http2 = { module = "io.netty:netty-codec-http2", version.ref = "netty4" }
//        netty-codec-socks = { module = "io.netty:netty-codec-socks", version.ref = "netty4" }
//        netty-common = { module = "io.netty:netty-common", version.ref = "netty4" }
//        netty-handler = { module = "io.netty:netty-handler", version.ref = "netty4" }
//        netty-handler-proxy = { module = "io.netty:netty-handler-proxy", version.ref = "netty4" }
//        netty-resolver = { module = "io.netty:netty-resolver", version.ref = "netty4" }
//        netty-resolver-dns = { module = "io.netty:netty-resolver-dns", version.ref = "netty4" }
//        netty-transport = { module = "io.netty:netty-transport", version.ref = "netty4" }
//        netty-transport-native-epoll = { module = "io.netty:netty-transport-native-epoll", version.ref = "netty4" }
//
//        caffeine = { module = "com.github.ben-manes.caffeine:caffeine", version = "2.9.2" }
//
//        httpclient = { module = "org.apache.httpcomponents:httpclient", version = "4.5.13" }
//        httpcore = { module = "org.apache.httpcomponents:httpcore", version = "4.4.14" }
//
//        # 2.7 (requires Java 8
//        # 2.6 (requires Java 7
//        commons-io = { module = "commons-io:commons-io", version = "2.11.0" }
//        # https://commons.apache.org/proper/commons-lang/changes-report.html
//        # Lang 3.9 and onwards now targets Java 8, making use of features that arrived with Java 8.
//        commons-lang3 = { module = "org.apache.commons:commons-lang3", version = "3.8.1" }
//        # for jdk6
//        commons-lang = { module = "commons-lang:commons-lang", version = "2.6" }
//        commons-collections = { module = "commons-collections:commons-collections", version = "3.2.2" }
//        commons-collections4 = { module = "org.apache.commons:commons-collections4", version = "4.4" }
//        commons-compress = { module = "org.apache.commons:commons-compress", version = "1.21" }
//        # Codec 1.14 (mirrors) requires Java 7
//        commons-codec = { module = "commons-codec:commons-codec", version = "1.14" }
//        commons-beanutils = { module = "commons-beanutils:commons-beanutils", version = "1.9.4" }
//        commons-math3 = { module = "org.apache.commons:commons-math3", version = "3.6.1" }
//        commons-digester = { module = "commons-digester:commons-digester", version = "2.1" }
//
//        jakarta-mail = { module = "com.sun.mail:jakarta.mail", version = "1.6.7" }
//
//        javassist = { module = "org.javassist:javassist", version = "3.27.0-GA" }
//        cglib-nodep = { module = "cglib:cglib-nodep", version = "3.2.12" }
//
//        asm-core = { module = "org.ow2.asm:asm", version.ref = "asm" }
//        asm-commons = { module = "org.ow2.asm:asm-commons", version.ref = "asm" }
//        asm-util = { module = "org.ow2.asm:asm-util", version.ref = "asm" }
//        asm-tree = { module = "org.ow2.asm:asm-tree", version.ref = "asm" }
//        asm-analysis = { module = "org.ow2.asm:asm-analysis", version.ref = "asm" }
//
//        aspectjweaver = { module = "org.aspectj:aspectjweaver", version = "1.9.5" }
//
//        javax-servlet-api-v3 = { module = "javax.servlet:javax.servlet-api", version = "3.0.1" }
//        javax-servlet-api = { module = "javax.servlet:javax.servlet-api", version = "4.0.1" }
//
//        # https://github.com/gradle/gradle/issues/15383 no support version catalog in prebuild script
//        junit = { module = "junit:junit", version = "4.13.2" }
//        awaitility = { module = "org.awaitility:awaitility", version = "3.1.5" }
//
//        zookeeper = { module = "org.apache.zookeeper:zookeeper", version = "3.4.14" }
//
//        curator-client = { module = "org.apache.curator:curator-client", version = "4.2.0" }
//        curator-framework = { module = "org.apache.curator:curator-framework", version = "4.2.1" }
//        curator-test = { module = "org.apache.curator:curator-test", version = "2.13.0" }
//
//        libthrift = { module = "org.apache.thrift:libthrift", version = "0.15.0" }
//        libthrift-v012 = { module = "org.apache.thrift:libthrift", version = "0.12.0" }
//
//        guava-jdk7 = { module = "com.google.guava:guava", version = "30.1-android" }
//        guava-jdk8 = { module = "com.google.guava:guava", version = "31.1-jre" }
//        guava-cassandra = { module = "com.google.guava:guava", version = "14.0.1" }
//        guice = { module = "com.google.inject:guice", version = "4.2.2" }
//
//        grpc-core = { module = "io.grpc:grpc-core", version.ref = "grpc" }
//        grpc-netty = { module = "io.grpc:grpc-netty", version.ref = "grpc" }
//        grpc-stub = { module = "io.grpc:grpc-stub", version.ref = "grpc" }
//        grpc-protobuf = { module = "io.grpc:grpc-protobuf", version.ref = "grpc" }
//        grpc-testing = { module = "io.grpc:grpc-testing", version.ref = "grpc" }
//
//        protobuf-java = { module = "com.google.protobuf:protobuf-java", version.ref = "protoc" }
//
//        jakarta-bind-api = { module = "jakarta.xml.bind:jakarta.xml.bind-api", version = "2.3.3" }
//        jakarta-jaxb-impl = { module = "com.sun.xml.bind:jaxb-impl", version = "2.3.6" }
//        jakarta-annotation-api = { module = "jakarta.annotation:jakarta.annotation-api", version = "1.3.5" }
//
//        tinylog-api = { module = "org.tinylog:tinylog-api", version.ref = "tinylog" }
//        tinylog-impl = { module = "org.tinylog:tinylog-impl", version.ref = "tinylog" }
//        tinylog-slf4j = { module = "org.tinylog:slf4j-tinylog", version.ref = "tinylog" }
//
//        nanohttpd = { module = "org.nanohttpd:nanohttpd", version = "2.3.1" }

    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["versionCatalog"])
        }
    }
}

group = "com.navercorp.pinpoint"