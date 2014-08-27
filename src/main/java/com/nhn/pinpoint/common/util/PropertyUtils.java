package com.nhn.pinpoint.common.util;

import java.io.*;
import java.util.Properties;

/**
 * @author emeroad
 */
public class PropertyUtils {

    public static Properties readProperties(String propertyPath) throws IOException {
        Properties properties = new Properties();
        InputStream in = null;
        Reader reader = null;
        try {
            in = new FileInputStream(propertyPath);
            reader = new InputStreamReader(in, "UTF-8");
            properties.load(reader);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {
                }
            }
        }
        return properties;
	}
}
