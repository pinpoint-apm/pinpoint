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

package com.navercorp.pinpoint.plugin.jdbc.oracle.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emeroad
 */
public interface KeyValue<V> {

    String getKey();

    V getValue();

    class TerminalKeyValue implements KeyValue<String> {

        public final String key;
        public final String value;

        public TerminalKeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "TerminalKeyValue{" +
                    "key='" + key + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    class KeyValueList implements KeyValue<List<KeyValue<?>>> {

        public final String key;
        public final List<KeyValue<?>> value;

        public KeyValueList(String key, List<KeyValue<?>> value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        public List<KeyValue<?>> getValue() {
            if (value == null) {
                return Collections.emptyList();
            }
            return value;
        }

        @Override
        public String toString() {
            return "KeyValueList{" +
                    "key='" + key + '\'' +
                    ", value=" + value +
                    '}';
        }
    }


    class Builder {
        public final String key;
        public String value;
        public List<KeyValue<?>> keyValueList;

        public Builder(String key) {
            this.key = key;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public void addKeyValueList(KeyValue<?> keyValue) {
            if (keyValueList == null) {
                this.keyValueList = new ArrayList<>();
            }
            this.keyValueList.add(keyValue);
        }

        public KeyValue build() {
            if (value != null) {
                return new TerminalKeyValue(key, value);
            }
            return new KeyValueList(key, keyValueList);
        }
    }

}
