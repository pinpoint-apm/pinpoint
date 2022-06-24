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

import com.navercorp.pinpoint.bootstrap.agentdir.JavaAgentPathResolver.ClassAgentPathFinder;
import com.navercorp.pinpoint.bootstrap.agentdir.JavaAgentPathResolver.InputArgumentAgentPathFinder;
import com.navercorp.pinpoint.common.Version;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JavaAgentPathResolverTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void testInputArgument() {
        Path agentPath = Paths.get("/pinpoint/agent/target/pinpoint-agent-" + Version.VERSION + "/pinpoint-bootstrap-" + Version.VERSION + ".jar");
        final List<String> inputArguments = Collections.singletonList(JavaAgentPathResolver.InputArgumentAgentPathFinder.JAVA_AGENT_OPTION + agentPath);

        JavaAgentPathResolver.AgentPathFinder javaAgentPathResolver = new InputArgumentAgentPathFinder() {
            @Override
            List<String> getInputArguments() {
                return inputArguments;
            }
        };
        Path resolveJavaAgentPath = javaAgentPathResolver.getPath();
        assertEquals(agentPath, resolveJavaAgentPath);
    }

    @Test
    public void testInputArgument2() {
        Path agentPath = Paths.get("C:/pinpoint/agent/target/pinpoint-agent-" + Version.VERSION + "/pinpoint-bootstrap-" + Version.VERSION + ".jar");
        final List<String> inputArguments = Collections.singletonList(JavaAgentPathResolver.InputArgumentAgentPathFinder.JAVA_AGENT_OPTION + agentPath);

        JavaAgentPathResolver.AgentPathFinder javaAgentPathResolver = new InputArgumentAgentPathFinder() {
            @Override
            List<String> getInputArguments() {
                return inputArguments;
            }
        };
        Path resolveJavaAgentPath = javaAgentPathResolver.getPath();
        assertEquals(agentPath, resolveJavaAgentPath);
    }

    @Test
    public void testClassAgentPath() {
        Class<Logger> clazz = Logger.class;

        ClassAgentPathFinder classAgentPath = new ClassAgentPathFinder();
        String jarLocation = classAgentPath.getJarLocation(clazz.getName());
        Path resolveTargetPath = Paths.get(URI.create(jarLocation));
        logger.debug("{}", resolveTargetPath);

        assertTrue(resolveTargetPath.getFileName().toString().endsWith(".jar"));

        URL classLocation = clazz.getProtectionDomain().getCodeSource().getLocation();
        Path classFile = getPath(classLocation);
        assertEquals(classFile, resolveTargetPath);
    }

    private Path getPath(URL classLocation) {
        File classFile = new File(classLocation.getFile());
        return Paths.get(classFile.getPath());
    }

}