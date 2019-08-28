/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.hbase.manager;

/**
 * @author HyunGil Jeong
 */
public enum Command {

    HELP("help"),
    INIT("init"),
    UPDATE("update"),
    RESET("reset"),
    SUMMARY("summary"),
    LOG("log");

    private final String value;

    Command(String value) {
        this.value = value;
    }

    public static Command fromValue(String value) {
        for (Command command : Command.values()) {
            if (command.value.equalsIgnoreCase(value)) {
                return command;
            }
        }
        return null;
    }
}
