/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yjqg6666
 */
enum BootLogLevel {

    ERROR(40, "ERROR"),
    WARN(30, "WARN"),
    INFO(20, "INFO"),
    DEBUG(10, "DEBUG"),
    TRACE(0, "TRACE");

    private static final Map<String, BootLogLevel> labelMap = new HashMap<>(5);

    private static final Map<Integer, BootLogLevel> valueMap = new HashMap<>(5);

    static {
        setup();
    }

    private final int value;

    private final String label;

    BootLogLevel(int value, String label) {
        this.label = label;
        this.value = value;
    }

    public static BootLogLevel of(String label) {
        return label != null ? labelMap.get(label.toUpperCase()) : null;
    }

    public static BootLogLevel of(int value) {
        return valueMap.get(value);
    }

    public boolean logTrace() {
        return checkLevel(TRACE);
    }

    public boolean logDebug() {
        return checkLevel(DEBUG);
    }

    public boolean logInfo() {
        return checkLevel(INFO);
    }

    public boolean logWarn() {
        return checkLevel(WARN);
    }

    public boolean logError() {
        return checkLevel(ERROR);
    }

    private boolean checkLevel(BootLogLevel check) {
        return check.value >= this.value;
    }

    private static void setup() {
        for (BootLogLevel i : values()) {
            labelMap.put(i.label, i);
            valueMap.put(i.value, i);
        }
    }

}
