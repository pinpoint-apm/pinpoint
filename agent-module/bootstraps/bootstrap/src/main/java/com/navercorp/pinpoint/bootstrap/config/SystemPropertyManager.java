package com.navercorp.pinpoint.bootstrap.config;

import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemPropertyManager {
    public static final String BACKUP_KEY = "profiler.sys.property.isolate";

    private final Map<String, String> backupMap = new HashMap<>(16);

    public void backup(AgentOption agentOption) {
        backup(agentOption.getProperties().getProperty(BACKUP_KEY));
    }

    public void backup(String propertyKeys) {
        for (String key : parsePropertyKeys(propertyKeys)) {
            final String systemProperty = System.getProperty(key);
            if (systemProperty != null) {
                // backup and clear
                backupMap.put(key, systemProperty);
                System.clearProperty(key);
            }
        }
    }

    List<String> parsePropertyKeys(String line) {
        if (StringUtils.isEmpty(line)) {
            return Collections.emptyList();
        }
        final List<String> list = new ArrayList<>();
        for (String key : line.split(",")) {
            final String trimmedKey = key.trim();
            if (Boolean.FALSE == StringUtils.isEmpty(trimmedKey)) {
                list.add(trimmedKey);
            }
        }
        return list;
    }

    public void restore() {
        for (Map.Entry<String, String> entry : backupMap.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            if (value != null) {
                // restore
                System.setProperty(key, value);
            }
        }
    }
}