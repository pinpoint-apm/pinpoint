package com.profiler.common.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtils {

    public static Properties readProperties(String propertyName) throws IOException {
        return readProperties(propertyName, PropertyUtils.class.getClassLoader());
    }

    public static Properties readProperties(String propertyName, ClassLoader cl) throws IOException {
		Properties properties = new Properties();
		InputStream stream = cl.getResourceAsStream(propertyName);
		if (stream == null) {
			throw new FileNotFoundException(propertyName + " not found.");
		}
		try {
			properties.load(stream);
		} finally {
            try {
                stream.close();
            } catch (IOException e) {
                // 무시
            }
		}
		return properties;
	}
}
