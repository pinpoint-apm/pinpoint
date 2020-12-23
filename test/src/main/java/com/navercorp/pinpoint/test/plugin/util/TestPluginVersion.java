package com.navercorp.pinpoint.test.plugin.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class TestPluginVersion {
    private static final String VERSION_PROPERTIES = "testplugin-version.properties";
    private static final String VERSION_KEY = "PROJECT_VERSION";

    private static final String VERSION = readVersion();

    private static String readVersion() {
        String projectVersion = readValue("PROJECT_VERSION");
        if (projectVersion == null) {
            throw new IllegalStateException(VERSION_KEY + " key not found");
        }
        return projectVersion;
    }

    private static String readValue(String key) {
        InputStream stream = TestPluginVersion.class.getClassLoader().getResourceAsStream(VERSION_PROPERTIES);
        Properties properties = loadProperties(stream);
        return properties.getProperty(key);
    }

    private static void close(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
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
            throw new RuntimeException(VERSION_PROPERTIES + " load failed");
        } finally {
            close(stream);
        }
    }

    public static String getVersion() {
        return VERSION;
    }
}
