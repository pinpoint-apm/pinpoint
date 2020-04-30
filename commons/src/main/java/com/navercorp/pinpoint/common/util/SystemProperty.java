/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.util;


import java.util.Set;

/**
 * @author emeroad
 */
public class SystemProperty implements SimpleProperty {

    public static final SystemProperty INSTANCE = new SystemProperty();

    @Override
    public void setProperty(String key, String value) {
        System.setProperty(key, value);
    }

    @Override
    public String getProperty(String key) {
        return System.getProperty(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }

    @Override
    public Set<String> stringPropertyNames() {
        return System.getProperties().stringPropertyNames();
    }


    public String getEnv(String name) {
        return System.getenv(name);
    }
}
