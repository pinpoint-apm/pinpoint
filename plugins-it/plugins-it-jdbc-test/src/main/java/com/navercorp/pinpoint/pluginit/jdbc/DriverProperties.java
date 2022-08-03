/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.pluginit.jdbc;

import com.navercorp.pinpoint.common.util.PropertyUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DriverProperties {

    private final Properties properties;

    public static final String URL = "url";
    public static final String USER = "user";
    public static final String PASSWARD = "password";
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String DATABASE = "database";
    private final String url;
    private final String user;
    private final String password;

    public static DriverProperties load(String propertyPath, String prefix) {
        Properties properties = load(propertyPath);

        PrefixReader reader = new PrefixReader(prefix, properties);
        String url = reader.getProperty(URL);
        String user = reader.getProperty(USER);
        String password = reader.getProperty(PASSWARD);

        return new DriverProperties(url, user, password, properties);
    }

    public DriverProperties(String url, String user, String password) {
        this(url, user, password, new Properties());
    }
    
    public DriverProperties(String url, String user, String password, Properties properties) {
        this.properties = properties;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private static class PrefixReader {
        private final Properties properties;
        private final String prefix;

        private PrefixReader(String prefix, Properties properties) {
            this.prefix = Objects.requireNonNull(prefix, "prefix");
            this.properties = properties;
        }

        private String getProperty(String postfix) {
            final String key = prefix + "." + postfix;
            final String value = properties.getProperty(key);
            if (value == null) {
                throw new IllegalArgumentException(key + " not found");
            }
            return value;
        }
    }

    private static Properties load(String propertyPath) {
        try {
            return PropertyUtils.loadPropertyFromClassPath(propertyPath);
        } catch (IOException ex) {
            throw new IllegalStateException(propertyPath + " load fail", ex);
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DriverProperties{");
        sb.append("properties=").append(properties);
        sb.append(", url='").append(url).append('\'');
        sb.append(", user='").append(user).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
