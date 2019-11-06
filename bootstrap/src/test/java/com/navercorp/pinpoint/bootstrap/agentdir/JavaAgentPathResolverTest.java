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

import com.navercorp.pinpoint.common.Version;
import org.junit.Test;

import java.lang.management.RuntimeMXBean;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JavaAgentPathResolverTest {

    @Test
    public void testInputArgument() {
        String agentPath = "/pinpoint/agent/target/pinpoint-agent-" + Version.VERSION + "/pinpoint-bootstrap-" + Version.VERSION + ".jar";
        final RuntimeMXBean runtimeMXBean = mock(RuntimeMXBean.class);
        List<String> inputArguments = Collections.singletonList(JavaAgentPathResolver.JAVA_AGENT_OPTION + agentPath);
        when(runtimeMXBean.getInputArguments()).thenReturn(inputArguments);

        JavaAgentPathResolver javaAgentPathResolver = new JavaAgentPathResolver(JavaAgentPathResolver.ResolvingType.INPUT_ARGUMENT) {
            @Override
            RuntimeMXBean getRuntimeMXBean() {
                return runtimeMXBean;
            }
        };
        String resolveJavaAgentPath = javaAgentPathResolver.resolveJavaAgentPath();
        org.junit.Assert.assertEquals(resolveJavaAgentPath, agentPath);
    }

}