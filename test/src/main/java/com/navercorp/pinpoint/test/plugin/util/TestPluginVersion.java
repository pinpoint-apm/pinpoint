package com.navercorp.pinpoint.test.plugin.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class TestPluginVersion {
    private static final String VERSION_PROPERTIES = "testplugin-version.properties";

    private static final String VERSION_KEY = "PROJECT_VERSION";
    private static final String NOT_COMPILED = "${project.version}";

    private static final String VERSION;

    static {
        InputStream stream = TestPluginVersion.class.getClassLoader().getResourceAsStream(VERSION_PROPERTIES);
        Properties properties = loadProperties(stream);

        VERSION = readString(properties, VERSION_KEY);
    }

    private static String readString(Properties properties, String key) {
        final String projectVersion = properties.getProperty(key);
        if (projectVersion == null) {
            throw new IllegalStateException(key + " key not found");
        }
        if (NOT_COMPILED.equals(projectVersion)) {
            throw new IllegalStateException("Install pinpoint-test module( $test> mvn install)");
        }
        return projectVersion;
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignore) {
            }
        }
    }

    private static Properties loadProperties(InputStream stream) {
        Properties properties = new Properties();
        try {
            properties.load(stream);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException("properties load failed", e);
        } finally {
            close(stream);
        }
    }

    public static String getVersion() {
        return VERSION;
    }
}
