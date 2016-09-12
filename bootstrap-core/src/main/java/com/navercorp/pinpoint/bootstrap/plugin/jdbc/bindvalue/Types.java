/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.bootstrap.plugin.jdbc.bindvalue;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author emeroad
 */
public class Types {

    private static final Map<Integer, String> MAP;

    static {
        MAP = inverse();
    }

    static Map<Integer, String> inverse() {
        Map<Integer, String> map = new HashMap<Integer, String>();
        Field[] fields = java.sql.Types.class.getFields();
        for (Field field : fields) {
            String name = field.getName();
            try {
                Integer value = (Integer) field.get(java.sql.Types.class);
                map.put(value, name);
            } catch (IllegalAccessException ignore) {
                // skip
            }
        }
        return map;
    }

    public static String findType(int type) {
        return MAP.get(type);
    }
}
