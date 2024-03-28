package com.navercorp.pinpoint.test.plugin;

import com.navercorp.pinpoint.common.Version;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public final class PluginClassLoading {

    private static final String[] CLASS_PATHS_TO_CHECK_AS_CONTAINS = new String[]{
            "junit", // JUnit
            "opentest4",
            "hamcrest", // for JUnit
            "assertj-core",
            "pinpoint-plugins-test", // pinpoint-test-{VERSION}.jar
            Paths.get("agent-module", "plugins-test", "target", "classes").toString(),
//            "/test/target/classes", // pinpoint-test build output directory
            Paths.get("testcase", "target", "classes").toString(),
            "pinpoint-testcase",
//            "/testcase/target/classes",
            Paths.get("pinpoint", "agent-module", "plugins").toString(),
//            "/pinpoint/plugins/", // required when executing test on IDE

            // logger for bootstrap classloader
            "tinylog-api",
            "tinylog-impl",
    };

    public static String[] getContainsCheckClassPath() {
        return CLASS_PATHS_TO_CHECK_AS_CONTAINS;
    }

    private static final String[] CLASS_PATHS_TO_CHECK_AS_GLOB_MATCHES = new String[]{
            "**" + File.separator + "pinpoint-*-plugin-" + Version.VERSION + ".jar", // required when executing test via mvn command
    };

    public static String[] getGlobMatchesCheckClassPath() {
        return CLASS_PATHS_TO_CHECK_AS_GLOB_MATCHES;
    }

    public static final String[] MAVEN_DEPENDENCY_CLASS_PATHS = new String[]{
            "maven-resolver",
            "maven-model",
            "maven-artifact",
            "maven-model-builder",
            "maven-builder-support",
            "maven-repository-metadata",
            "commons-lang3",
            "apache/maven",
            "commons-logging",
            "httpclient",
            "httpcore",
            "guava",
            "plexus",
            "pinpoint-plugins-test",
            Paths.get("agent-module", "plugins-test", "target", "classes").toString(),
//            "/test/target/classes", // pinpoint-test build output directory

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

    public static final List<String> LOGGER_DEPENDENCY = format(LOGGER_DEPENDENCY_ID, LOG4J2_VERSION);

    public static List<String> format(String[] libs, String version) {
        String[] loggers = new String[libs.length];
        for (int i = 0; i < libs.length; i++) {
            String lib = libs[i];
            loggers[i] = String.format(lib, version);
        }
        return Arrays.asList(loggers);
    }

    public static final String[] PLUGIN_GLOB_MATCHES = new String[]{
            "**" + File.separator + "pinpoint-*-plugin-" + Version.VERSION + ".jar", // required when executing test via mvn command
            "**" + File.separator + "naver-pinpoint-*-plugin-" + Version.VERSION + ".jar" // required when executing test via mvn command
    };

    public static final String[] PLUGIN_CONTAINS_MATCHES = new String[]{
            File.separator + "pinpoint" + File.separator + "agent-module" + File.separator + "plugins" + File.separator,
            File.separator + "pinpoint-naver" + File.separator + "agent-module" + File.separator + "naver-plugins" + File.separator
    };

    public static final String[] PLUGIN_IT_UTILS_CONTAINS_MATCHES = new String[]{
            "pinpoint-plugin-it-utils", // maven
            "plugins-it-utils", // ide
            "pinpoint-plugin-it-utils-jdbc", // maven
            "plugin-it-utils-jdbc", // ide
            "naver-pinpoint-plugin-it-utils",
            "naver-plugins-it-utils"
    };

    public static final String[] JUNIT_CONTAINS_MATCHES = new String[]{
            "junit", // JUnit
            "opentest4",
            "hamcrest", // for JUnit
            "assertj-core",
            "pinpoint-test", // pinpoint-test-{VERSION}.jar
            Paths.get("test", "target", "classes").toString(),
            "pinpoint-testcase",
            Paths.get("testcase", "target", "classes").toString(),
            // logger for bootstrap classloader
            "tinylog-api",
            "tinylog-impl",
    };

    public static final String[] TEST_MATCHES = new String[]{
            "pinpoint-test", // pinpoint-test-{VERSION}.jar
            Paths.get("test", "target", "classes").toString(),
            "pinpoint-testcase",
            Paths.get("testcase", "target").toString(),
            Paths.get("testcase", "target", "classes").toString()
    };
}