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
public class KeyValue {

    public String key;
    public String value;
    public List<KeyValue> keyValueList;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<KeyValue> getKeyValueList() {
        if (keyValueList == null) {
            return Collections.emptyList();
        }
        return keyValueList;
    }

    public void addKeyValueList(KeyValue keyValue) {
        if (keyValueList == null) {
            keyValueList = new ArrayList<KeyValue>();
        }
        this.keyValueList.add(keyValue);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{key='").append(key).append('\'');
        if (value != null) {
            sb.append(", value='").append(value).append('\'');
        }
        if (keyValueList != null) {
            sb.append(", keyValueList=").append(keyValueList);
        }
        sb.append('}');
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyValue keyValue = (KeyValue) o;

        if (key != null ? !key.equals(keyValue.key) : keyValue.key != null) return false;
        if (keyValueList != null ? !keyValueList.equals(keyValue.keyValueList) : keyValue.keyValueList != null) return false;
        if (value != null ? !value.equals(keyValue.value) : keyValue.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (keyValueList != null ? keyValueList.hashCode() : 0);
        return result;
    }
}
