package com.profiler.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtils {

    public static Properties readProperties(String propertyName) {
        return readProperties(propertyName, PropertyUtils.class.getClassLoader());
    }

    public static Properties readProperties(String propertyName, ClassLoader cl) {
		Properties properties = new Properties();
		InputStream stream = cl.getResourceAsStream(propertyName);
		if (stream == null) {
			throw new RuntimeException(propertyName + " not found.");
		}
		try {
			properties.load(stream);
		} catch (IOException e) {
			throw new RuntimeException(propertyName + " load fail. Cause:" + e.getMessage(), e);
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
