/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.io.header;

import java.util.Collections;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class HeaderEntity {

    public static final HeaderEntity EMPTY_HEADER_ENTITY = new HeaderEntity(Collections.<String, String>emptyMap());

    private final Map<String, String> entity;

    public HeaderEntity(Map<String, String> headerEntityData) {
        if (headerEntityData == null) {
            throw new NullPointerException("headerEntityData");
        }

        this.entity = Collections.unmodifiableMap(headerEntityData);
    }

    public String getEntity(String key) {
        return entity.get(key);
    }

    public Map<String, String> getEntityAll() {
        return entity;
    }

    @Override
    public String toString() {
        return "HeaderEntity{" +
                "entity=" + entity +
                '}';
    }
}
