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

import com.navercorp.pinpoint.common.Version;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author emeroad
 */
@Ignore
public class AgentDirBaseClassPathResolverTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // ---------------------------
    // setup Agent build dir
    private String agentBuildDir = "___setup Agent build dir___";
    // ---------------------------

    private String testBootStrapJar = "pinpoint-bootstrap-" + Version.VERSION + ".jar";
    private String agentBootstrapPath = agentBuildDir + File.separator + testBootStrapJar;

    @Test
    public void testFindAgentJar() throws Exception {

        logger.debug("testAgentDir:{}", agentBuildDir);
        logger.debug("agentBootstrapPath:{}", agentBootstrapPath);

        AgentDirBaseClassPathResolver classPathResolver = new AgentDirBaseClassPathResolver(agentBootstrapPath);
        Assert.assertTrue("verify agent directory ", classPathResolver.verify());

        boolean findAgentJar = classPathResolver.findAgentJar();
        Assert.assertTrue(findAgentJar);

        String agentJar = classPathResolver.getAgentJarName();
        Assert.assertEquals(testBootStrapJar, agentJar);

        String agentPath = classPathResolver.getAgentJarFullPath();
        Assert.assertEquals(agentBootstrapPath, agentPath);

        String agentDirPath = classPathResolver.getAgentDirPath();
        Assert.assertEquals(agentBuildDir, agentDirPath);

        String agentLibPath = classPathResolver.getAgentLibPath();
        Assert.assertEquals(agentBuildDir + File.separator + "lib", agentLibPath);
    }


    @Test
    public void findAgentJar() {
        logger.debug("testAgentDir:{}", agentBuildDir);
        logger.debug("agentBootstrapPath:{}", agentBootstrapPath);

        findAgentJar(agentBootstrapPath);


        findAgentJarAssertFail(agentBuildDir + File.separator + "pinpoint-bootstrap-unknown.jar");
    }

    private void findAgentJar(String path) {
        AgentDirBaseClassPathResolver classPathResolver = new AgentDirBaseClassPathResolver(path);
        boolean agentJar = classPathResolver.findAgentJar();
        Assert.assertTrue(agentJar);
    }

    private void findAgentJarAssertFail(String path) {
        AgentDirBaseClassPathResolver classPathResolver = new AgentDirBaseClassPathResolver(path);
        boolean agentJar = classPathResolver.findAgentJar();
        Assert.assertFalse(agentJar);
    }

}

