package com.navercorp.pinpoint.bootstrap.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyLoaderUtils {

    public static final String[] ALLOWED_PROPERTY_PREFIX = new String[]{"bytecode.", "profiler.", "pinpoint."};

    public static Properties loadFileProperties(Path filePath) {
        Properties properties = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("%s load fail Caused by:%s", filePath, e.getMessage()), e);
        }
        return properties;
    }


    public static <K, V> Map<K, V> filterAllowedPrefix(Map<K, V> properties) {
        final Map<K, V> copy = new HashMap<>();
        for (Map.Entry<K, V> entry : properties.entrySet()) {
            final K key = entry.getKey();
            final V value = entry.getValue();
            if (key instanceof String && value instanceof String) {
                final String name = (String) key;
                if (filter(name)) {
                    copy.put(key, value);
                }
            }
        }
        return copy;
    }

    private static boolean filter(String name) {
        for (String prefix : ALLOWED_PROPERTY_PREFIX) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
