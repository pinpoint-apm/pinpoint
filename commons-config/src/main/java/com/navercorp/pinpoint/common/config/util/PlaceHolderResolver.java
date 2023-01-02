package com.navercorp.pinpoint.common.config.util;

import com.navercorp.pinpoint.common.config.util.spring.PropertyPlaceholderHelper;

import java.util.Objects;
import java.util.Properties;

public class PlaceHolderResolver implements ValueResolver {
    private final Properties properties;
    private final PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper(PlaceHolder.START, PlaceHolder.END);

    public PlaceHolderResolver(Properties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    @Override
    public String resolve(String key, String value) {
        if (value == null) {
            return null;
        }
        return propertyPlaceholderHelper.replacePlaceholders(value, properties);
    }
}
