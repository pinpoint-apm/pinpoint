package com.navercorp.pinpoint.bootstrap.config;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;

/**
 * @author Woonduk Kang(emeroad)
 */
public class OsEnvSimpleProperty {

    private final Function<String, String> rule;

    public OsEnvSimpleProperty() {
        this(OsEnvSimpleProperty::resolveKey);
    }

    public OsEnvSimpleProperty(Function<String, String> rule) {
        this.rule = Objects.requireNonNull(rule, "rule");
    }

    public Properties toProperties(Map<String, String> envMap) {
        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : envMap.entrySet()) {
            String key = rule.apply(entry.getKey());
            String value = entry.getValue();
            properties.setProperty(key, value);
        }
        return properties;
    }

    public static String resolveKey(String key) {
        return key.replace('_', '.').toLowerCase();
    }

}
