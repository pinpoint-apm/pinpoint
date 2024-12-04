/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.Properties;

import static org.mockito.Mockito.mock;

/**
 * @author emeroad
 */
public class AgentBootLoaderTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void boot() {
        boot("testAgentName");
    }

    @Test
    public void bootNoAgentName() {
        boot("testNoAgentName");
    }

    private void boot(String agentName) {
        ClassLoader classLoader = AgentBootLoaderTest.class.getClassLoader();
        AgentBootLoader agentBootLoader = new AgentBootLoader("com.navercorp.pinpoint.bootstrap.DummyAgent", classLoader);
        Instrumentation instrumentation = mock(Instrumentation.class);
        AgentOption option = new DefaultAgentOption(instrumentation,
                new Properties(), Collections.emptyMap(), Collections.emptyList(), Collections.emptyList());
        Object boot = agentBootLoader.boot(option);
        try {
            Class<?> agentClazz = boot.getClass();
            agentClazz.getMethod("start").invoke(boot);
            agentClazz.getMethod("close").invoke(boot);
        } catch (Exception e) {
            throw new RuntimeException("agent boot failed", e);
        }
    }

}
