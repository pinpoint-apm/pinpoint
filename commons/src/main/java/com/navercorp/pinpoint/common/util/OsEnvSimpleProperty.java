package com.navercorp.pinpoint.common.util;

import java.util.Map;
import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public class OsEnvSimpleProperty extends PropertySnapshot {

    public OsEnvSimpleProperty(Map<String, String> envMap) {
        super(copy(envMap));
    }

    private static Properties copy(Map<String, String> envMap) {
        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : envMap.entrySet()) {
            String key = resolveKey(entry.getKey());
            String value = entry.getValue();
            properties.setProperty(key, value);
        }
        return properties;
    }

    private static String resolveKey(String key) {
        return key.replace('_', '.').toLowerCase();
    }

}
