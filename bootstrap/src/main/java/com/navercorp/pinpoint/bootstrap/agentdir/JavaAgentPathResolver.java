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

import com.navercorp.pinpoint.bootstrap.BootLogger;
import com.navercorp.pinpoint.common.annotations.VisibleForTesting;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JavaAgentPathResolver {

    private final AgentPathFinder[] agentPathFinders;

    JavaAgentPathResolver(AgentPathFinder[] agentPathFinders) {
        if (agentPathFinders == null) {
            throw new NullPointerException("agentPathFinders");
        }
        this.agentPathFinders = agentPathFinders;
    }

    public static JavaAgentPathResolver newJavaAgentPathResolver() {
        final AgentPathFinder[] agentPathFinders = newAgentPathFinder();
        return new JavaAgentPathResolver(agentPathFinders);
    }

    private static AgentPathFinder[] newAgentPathFinder() {
        AgentPathFinder classAgentPath = new ClassAgentPathFinder();
        AgentPathFinder inputArgumentAgentPath = new InputArgumentAgentPathFinder();
        return new AgentPathFinder[] {classAgentPath, inputArgumentAgentPath};
    }

    public String resolveJavaAgentPath() {
        for (AgentPathFinder agentPath : agentPathFinders) {
            final String path = agentPath.getPath();
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    interface AgentPathFinder {
        String getPath();
    }

    static class ClassAgentPathFinder implements AgentPathFinder {
        private final BootLogger logger = BootLogger.getLogger(ClassAgentPathFinder.class.getName());

        private final String className;

        public ClassAgentPathFinder() {
            this("com.navercorp.pinpoint.bootstrap.PinpointBootStrap");
        }

        public ClassAgentPathFinder(String className) {
            this.className = className;
        }

        @Override
        public String getPath() {
            // get bootstrap.jar location
            return getJarLocation(this.className);
        }

        @VisibleForTesting
        String getJarLocation(String className) {
            final String internalClassName = className.replace('.', '/') + ".class";
            final URL classURL = getResource(internalClassName);
            if (classURL == null) {
                return null;
            }

            if (classURL.getProtocol().equals("jar")) {
                String path = classURL.getPath();
                int jarIndex = path.indexOf("!/");
                if (jarIndex == -1) {
                    throw new IllegalArgumentException("!/ not found " + path);
                }
                final String agentPath = path.substring("file:".length(), jarIndex);
                logger.info("agentPath:" + agentPath);
                return agentPath;
           }
            // unknown
            return null;
        }

        private URL getResource(String internalClassName) {
            return ClassLoader.getSystemResource(internalClassName);
        }

    }

    @Deprecated
    static class InputArgumentAgentPathFinder implements AgentPathFinder {

        private final BootLogger logger = BootLogger.getLogger(InputArgumentAgentPathFinder.class.getName());
        
        static final String JAVA_AGENT_OPTION = "-javaagent:";
        
        private final Pattern DEFAULT_AGENT_PATTERN = AgentDirBaseClassPathResolver.bootstrap.getVersionPattern();

        @Override
        public String getPath() {
            final List<String> inputArguments = getInputArguments();
            for (String inputArgument : inputArguments) {
                if (isPinpointAgent(inputArgument, DEFAULT_AGENT_PATTERN)) {
                    String agentPath = removeJavaAgentPrefix(inputArgument);
                    logger.info("agentPath:" + agentPath);
                    return agentPath;
                }
            }
            return null;
        }

        @VisibleForTesting
        List<String> getInputArguments() {
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            return runtimeMXBean.getInputArguments();
        }


        private boolean isPinpointAgent(String inputArgument, Pattern javaPattern) {
            if (!inputArgument.startsWith(JAVA_AGENT_OPTION)) {
                return false;
            }
            Matcher matcher = javaPattern.matcher(inputArgument);
            return matcher.find();
        }

        private String removeJavaAgentPrefix(String inputArgument) {
            return inputArgument.substring(JAVA_AGENT_OPTION.length(), inputArgument.length());
        }
    }

}
