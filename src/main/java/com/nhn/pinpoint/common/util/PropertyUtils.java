package com.nhn.pinpoint.common.util;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PropertyUtils {

    public static Properties readProperties(String propertyPath) throws IOException {
        Properties properties = new Properties();
        FileReader fileReader = new FileReader(propertyPath);
		try {
			properties.load(fileReader);
		} finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                // 무시
            }
		}
		return properties;
	}
}
