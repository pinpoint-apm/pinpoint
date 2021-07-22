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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collections;
import java.util.List;


/**
 * @author Woonduk Kang(emeroad)
 */
public class JavaAgentPathResolverTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Test
    public void testInputArgument() {
        String agentPath = "/pinpoint/agent/target/pinpoint-agent-" + Version.VERSION + "/pinpoint-bootstrap-" + Version.VERSION + ".jar";
        final List<String> inputArguments = Collections.singletonList(JavaAgentPathResolver.InputArgumentAgentPathFinder.JAVA_AGENT_OPTION + agentPath);

        JavaAgentPathResolver.AgentPathFinder javaAgentPathResolver = new InputArgumentAgentPathFinder() {
            @Override
            List<String> getInputArguments() {
                return inputArguments;
            }
        };
        String resolveJavaAgentPath = javaAgentPathResolver.getPath();
        org.junit.Assert.assertEquals(resolveJavaAgentPath, agentPath);
    }

    @Test
    public void testClassAgentPath() {
        Class<Logger> clazz = Logger.class;

        ClassAgentPathFinder classAgentPath = new ClassAgentPathFinder();
        String resolveTargetPath = classAgentPath.getJarLocation(clazz.getName());
        logger.debug("{}", resolveTargetPath);
        org.junit.Assert.assertTrue(resolveTargetPath.endsWith(".jar"));

        URL location = clazz.getProtectionDomain().getCodeSource().getLocation();
        org.junit.Assert.assertEquals(resolveTargetPath, location.getPath());
    }

}