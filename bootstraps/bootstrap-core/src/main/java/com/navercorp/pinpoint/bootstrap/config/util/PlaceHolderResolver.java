package com.navercorp.pinpoint.bootstrap.config.util;

import com.navercorp.pinpoint.bootstrap.util.spring.PropertyPlaceholderHelper;

import java.util.Properties;

public class PlaceHolderResolver implements ValueResolver {
    private final Properties properties;
    private final PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper(PlaceHolder.START, PlaceHolder.END);

    public PlaceHolderResolver(Properties properties) {
        if (properties == null) {
            throw new NullPointerException("properties");
        }
        this.properties = properties;
    }

    @Override
    public String resolve(String key, String value) {
        if (value == null) {
            return null;
        }
        return propertyPlaceholderHelper.replacePlaceholders(value, properties);
    }
}
