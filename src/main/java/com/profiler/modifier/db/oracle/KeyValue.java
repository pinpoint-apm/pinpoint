package com.profiler.modifier.db.oracle;

import java.util.ArrayList;
import java.util.List;

/**
 *
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
}
