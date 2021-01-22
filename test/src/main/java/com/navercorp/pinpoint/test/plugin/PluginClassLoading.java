package com.navercorp.pinpoint.test.plugin;

public final class PluginClassLoading {

    public static final String[] REQUIRED_CLASS_PATHS = new String[]{
            "junit", // JUnit
            "hamcrest-core", // for JUnit
            "pinpoint-test", // pinpoint-test-{VERSION}.jar
            "/test/target/classes", // pinpoint-test build output directory
            "/testcase/target/classes",

            // logger for bootstrap classloader
            "tinylog-api",
            "tinylog-impl",
    };

    public static final String[] MAVEN_DEPENDENCY_CLASS_PATHS = new String[]{
            "maven-resolver",
            "commons-lang3",
            "apache/maven",
            "guava",
            "plexus",
            "pinpoint-test",
            "/test/target/classes", // pinpoint-test build output directory

            // logger for child classloader
            "slf4j-api", // slf4j-api
            "slf4j-tinylog"
    };

    private static final String LOG4J2_VERSION = "2.12.1";
    private static final String[] LOGGER_DEPENDENCY_ID = new String[]{
            "org.apache.logging.log4j:log4j-api:%s",
            "org.apache.logging.log4j:log4j-core:%s",
            "org.apache.logging.log4j:log4j-slf4j-impl:%s"
    };

    public static final String[] LOGGER_DEPENDENCY = format(LOGGER_DEPENDENCY_ID, LOG4J2_VERSION);

    public static String[] format(String[] libs, String version) {
        String[] loggers = new String[libs.length];
        for (int i = 0; i < libs.length; i++) {
            String lib = libs[i];
            loggers[i] = String.format(lib, version);
        }
        return loggers;
    }





}
