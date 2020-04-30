/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.util;

import java.util.Properties;
import java.util.Set;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PropertySnapshot implements SimpleProperty {
    private final Properties properties;

    public PropertySnapshot(Properties properties) {
        this.properties = copy(properties);
    }

    private Properties copy(Properties properties) {
        if (properties == null) {
            throw new NullPointerException("properties must not be null");
        }
        final Set<String> keys = properties.stringPropertyNames();
        final Properties copy = new Properties();
        for (String key : keys) {
            copy.setProperty(key, properties.getProperty(key));
        }
        return copy;
    }

    @Override
    public void setProperty(String key, String value) {
        this.properties.setProperty(key, value);
    }

    @Override
    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return this.properties.getProperty(key, defaultValue);
    }

    @Override
    public Set<String> stringPropertyNames() {
        return this.properties.stringPropertyNames();
    }
}
