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

package com.navercorp.pinpoint.bootstrap.agentdir;

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JavaAgentPathResolver {

    static final String JAVA_AGENT_OPTION = "-javaagent:";
    private final Pattern DEFAULT_AGENT_PATTERN = AgentDirBaseClassPathResolver.bootstrap.getVersionPattern();

    private final ResolvingType resolvingType;

    enum ResolvingType {
        INPUT_ARGUMENT,
        // Added for unknown bugs.
        @Deprecated
        SYSTEM_PROPERTY
    };

    JavaAgentPathResolver(ResolvingType resolvingType) {
        if (resolvingType == null) {
            throw new NullPointerException("type must not be null");
        }
        this.resolvingType = resolvingType;
    }

    public static JavaAgentPathResolver newJavaAgentPathResolver() {
        final ResolvingType resolvingType = getResolvingType();
        return new JavaAgentPathResolver(resolvingType);
    }

    private static ResolvingType getResolvingType() {
        final String type = System.getProperty("pinpoint.javaagent.resolving", "");
        if (type.equalsIgnoreCase("system")) {
            return ResolvingType.SYSTEM_PROPERTY;
        }
        return ResolvingType.INPUT_ARGUMENT;
    }

    public String resolveJavaAgentPath() {
        if (resolvingType == ResolvingType.SYSTEM_PROPERTY) {
            return getClassPathFromSystemProperty();
        }

        RuntimeMXBean runtimeMXBean = getRuntimeMXBean();
        List<String> inputArguments = runtimeMXBean.getInputArguments();
        for (String inputArgument : inputArguments) {
            if (isPinpointAgent(inputArgument, DEFAULT_AGENT_PATTERN)) {
                return removeJavaAgentPrefix(inputArgument);
            }
        }
        throw new IllegalArgumentException(JAVA_AGENT_OPTION + " not found");
    }

    @VisibleForTesting
    RuntimeMXBean getRuntimeMXBean() {
        return ManagementFactory.getRuntimeMXBean();
    }

    private String removeJavaAgentPrefix(String inputArgument) {
        return inputArgument.substring(JAVA_AGENT_OPTION.length(), inputArgument.length());
    }

    private boolean isPinpointAgent(String inputArgument, Pattern javaPattern) {
        if (!inputArgument.startsWith(JAVA_AGENT_OPTION)) {
            return false;
        }
        Matcher matcher = javaPattern.matcher(inputArgument);
        return matcher.find();
    }

    String getClassPathFromSystemProperty() {
        return System.getProperty("java.class.path");
    }

}
