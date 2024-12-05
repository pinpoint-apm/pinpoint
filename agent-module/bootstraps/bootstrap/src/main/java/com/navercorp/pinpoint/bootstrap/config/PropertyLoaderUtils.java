package com.navercorp.pinpoint.bootstrap.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class PropertyLoaderUtils {
    static void loadFileProperties(Properties properties, Path filePath) {
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("%s load fail Caused by:%s", filePath, e.getMessage()), e);
        }
    }
}
