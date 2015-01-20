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


import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Collections;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.common.plugin.Plugins;

/**
 * @author emeroad
 */
public class AgentClassLoaderTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void boot() throws IOException, ClassNotFoundException {
        AgentClassLoader agentClassLoader = new AgentClassLoader(new URL[0]);
        agentClassLoader.setBootClass("com.navercorp.pinpoint.bootstrap.DummyAgent");
        agentClassLoader.boot("test", new DummyInstrumentation(), new ProfilerConfig(), new Plugins<ProfilerPlugin>(Collections.<ProfilerPlugin>emptyList(), new URL[0]));
        // TODO need verification - implementation for obtaining logger changed
//        PLoggerBinder loggerBinder = (PLoggerBinder) agentClassLoader.initializeLoggerBinder();
//        PLogger test = loggerBinder.getLogger("test");
//        test.info("slf4j logger test");

    }

    private String getProjectLibDir() {
        // not really necessary, but useful for testing protectionDomain
        ProtectionDomain protectionDomain = AgentClassLoader.class.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URL location = codeSource.getLocation();

        logger.info("lib location:" + location);
        String path = location.getPath();
        // file:/D:/nhn_source/pinpoint_project/pinpoint-tomcat-profiler/target/classes/
        int dirPath = path.lastIndexOf("target/classes/");
        if (dirPath == -1) {
            throw new RuntimeException("target/classes/ not found");
        }
        String projectDir = path.substring(1, dirPath);
        return projectDir + "src/test/lib";
    }
}
