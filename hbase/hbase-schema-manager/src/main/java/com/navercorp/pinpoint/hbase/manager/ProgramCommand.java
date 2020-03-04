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

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class ProgramCommand {

    public static final ProgramCommand EMPTY = new ProgramCommand("", Collections.emptyList());

    private final String command;
    private final List<String> commandArgs;

    private ProgramCommand(String command) {
        this(command, Collections.emptyList());
    }

    private ProgramCommand(String command, List<String> commandArgs) {
        this.command = Objects.requireNonNull(command, "command");
        this.commandArgs = Objects.requireNonNull(commandArgs, "commandArgs");
    }

    public static ProgramCommand parseArgs(String[] args) {
        if (args == null) {
            throw new NullPointerException("args");
        }
        return parseArgs(new DefaultApplicationArguments(args));
    }

    public static ProgramCommand parseArgs(ApplicationArguments applicationArguments) {
        if (applicationArguments == null) {
            throw new NullPointerException("applicationArguments");
        }
        List<String> nonOptionArgs = applicationArguments.getNonOptionArgs();
        if (CollectionUtils.isEmpty(nonOptionArgs)) {
            return EMPTY;
        }
        String commandString = nonOptionArgs.get(0);
        if (nonOptionArgs.size() > 1) {
            List<String> commandArgs = new ArrayList<>(nonOptionArgs.subList(1, nonOptionArgs.size()));
            return new ProgramCommand(commandString, commandArgs);
        }
        return new ProgramCommand(commandString);
    }

    public String getCommand() {
        return command;
    }

    public List<String> getCommandArgs() {
        return commandArgs;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder().append('[').append(command).append(']');
        if (!commandArgs.isEmpty()) {
            sb.append(", args : ").append(commandArgs);
        }
        return sb.toString();
    }
}
