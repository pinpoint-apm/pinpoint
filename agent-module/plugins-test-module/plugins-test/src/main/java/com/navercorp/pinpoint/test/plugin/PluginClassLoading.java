package com.navercorp.pinpoint.test.plugin;

import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.test.plugin.util.VersionUtils;

import java.util.Arrays;
import java.util.List;

import static com.navercorp.pinpoint.test.plugin.util.PathUtils.path;
import static com.navercorp.pinpoint.test.plugin.util.PathUtils.wrapPath;

public final class PluginClassLoading {

    private static final String VERSION = VersionUtils.VERSION;

    private static final String[] CLASS_PATHS_TO_CHECK_AS_CONTAINS = new String[]{
            "junit", // JUnit
            "opentest4",
            "hamcrest", // for JUnit
            "assertj-core",
            "pinpoint-plugins-test", // pinpoint-test-{VERSION}.jar
            path("agent-module", "plugins-test-module", "plugins-test", "target", "classes"),
//            "/test/target/classes", // pinpoint-test build output directory
            path("testcase", "target", "classes"),
            "pinpoint-testcase",
//            "/testcase/target/classes",
            path("pinpoint", "agent-module", "plugins"),
//            "/pinpoint/plugins/", // required when executing test on IDE

            // logger for bootstrap classloader
            "tinylog-api",
            "tinylog-impl",
    };

    private static final String[] CLASS_PATHS_TO_SHARED = new String[]{
            "junit", // JUnit
            "opentest4",
            "hamcrest", // for JUnit
            "assertj-core",
            "pinpoint-plugins-test", // pinpoint-test-{VERSION}.jar
            path("agent-module", "plugins-test-module", "plugins-test", "target", "classes"),
//            "/test/target/classes", // pinpoint-test build output directory
            path("testcase", "target", "classes"),
            "pinpoint-testcase",
//            "/testcase/target/classes",
            // logger for bootstrap classloader
            "tinylog-api",
            "tinylog-impl",
    };


    public static String[] getContainsCheckClassPath() {
        return CLASS_PATHS_TO_CHECK_AS_CONTAINS;
    }

    public static String[] getContainsCheckSharedClassPath() {
        return CLASS_PATHS_TO_SHARED;
    }

    private static final String[] CLASS_PATHS_TO_CHECK_AS_GLOB_MATCHES = new String[]{
            // required when executing test via mvn command
            path("**", "pinpoint-*-plugin-" + Version.VERSION + ".jar")
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
            path("agent-module", "plugins-test-module", "plugins-test", "target", "classes"),
//            "/test/target/classes", // pinpoint-test build output directory

            // logger for child classloader
            "slf4j-api", // slf4j-api
            "slf4j-tinylog"
    };

    private static final String LOG4J2_VERSION = PluginEngineTemplate.LOG4J_VERSION;
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
            // required when executing test via mvn command
            path("**", "pinpoint-*-plugin-" + VERSION + ".jar"),
            path("**", "naver-pinpoint-*-plugin-" + VERSION + ".jar")
    };

    public static final String[] PLUGIN_CONTAINS_MATCHES = new String[]{
            wrapPath("pinpoint", "agent-module", "plugins"),
            wrapPath("pinpoint-naver", "agent-module", "naver-plugins")
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
            path("test", "target", "classes"),
            "pinpoint-testcase",
            path("testcase", "target", "classes"),
            // logger for bootstrap classloader
            "tinylog-api",
            "tinylog-impl",
    };

    public static final String[] TEST_MATCHES = new String[]{
            "pinpoint-test", // pinpoint-test-{VERSION}.jar
            path("test", "target", "classes"),
            "pinpoint-testcase",
            path("testcase", "target"),
            path("testcase", "target", "classes")
    };

}
