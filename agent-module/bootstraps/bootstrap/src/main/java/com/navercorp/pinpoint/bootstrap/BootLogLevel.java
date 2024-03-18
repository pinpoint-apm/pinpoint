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

/**
 * @author yjqg6666
 */
enum BootLogLevel {

    ERROR(40),
    WARN(30),
    INFO(20),
    DEBUG(10),
    TRACE(0);

    private final int value;

    BootLogLevel(int value) {
        this.value = value;
    }

    public static BootLogLevel of(String label) {
        if (label == null) {
            return null;
        }
        try {
            return BootLogLevel.valueOf(label);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static BootLogLevel of(int value) {
        switch (value) {
            case 40:
                return ERROR;
            case 30:
                return WARN;
            case 20:
                return INFO;
            case 10:
                return DEBUG;
            case 0:
                return TRACE;
        }
        return null;
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


}
