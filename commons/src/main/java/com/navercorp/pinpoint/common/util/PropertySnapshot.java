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

import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PropertySnapshot {

    private PropertySnapshot() {
    }

    public static Properties copy(Properties properties) {
        Objects.requireNonNull(properties, "properties");

        final Set<String> keys = properties.stringPropertyNames();
        final Properties copy = new Properties();
        for (String key : keys) {
            copy.setProperty(key, properties.getProperty(key));
        }
        return copy;
    }

}
