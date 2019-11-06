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

import com.navercorp.pinpoint.bootstrap.AgentDirGenerator;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.util.CodeSourceUtils;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarFile;

/**
 * @author emeroad
 */
public class AgentDirBaseClassPathResolverTest {

    private static final Logger logger = LoggerFactory.getLogger(AgentDirBaseClassPathResolverTest.class);

    private static final String BOOTSTRAP_JAR = "pinpoint-bootstrap-" + Version.VERSION + ".jar";
    private static final String TEST_AGENT_DIR = "testagent";
    private static final String SEPARATOR = File.separator;

    private static final AtomicInteger AGENT_ID_ALLOCATOR = new AtomicInteger();

    private static String agentBuildDir;
    private static String agentBootstrapPath;

    private static AgentDirGenerator agentDirGenerator;

    @BeforeClass
    public static void beforeClass() throws Exception {

        String classLocation = getClassLocation(AgentDirBaseClassPathResolverTest.class);
        logger.debug("buildDir:{}", classLocation);

        agentBuildDir = classLocation + SEPARATOR + TEST_AGENT_DIR + '_' + AGENT_ID_ALLOCATOR.incrementAndGet();

        logger.debug("agentBuildDir:{}", agentBuildDir);

        agentBootstrapPath = agentBuildDir + SEPARATOR + BOOTSTRAP_JAR;

        logger.debug("agentBootstrapPath:{}", agentBootstrapPath);

        createAgentDir(agentBuildDir);


    }

    private static void createAgentDir(String tempAgentDir) throws IOException {

        agentDirGenerator = new AgentDirGenerator(tempAgentDir);
        agentDirGenerator.create();

    }


    @AfterClass
    public static void afterClass() throws Exception {
        if (agentDirGenerator != null) {
            agentDirGenerator.remove();
        }
    }

    @Test
    public void testFindAgentJar() throws Exception {

        logger.debug("TEST_AGENT_DIR:{}", agentBuildDir);
        logger.debug("agentBootstrapPath:{}", agentBootstrapPath);

        AgentDirBaseClassPathResolver classPathResolver = new AgentDirBaseClassPathResolver(agentBootstrapPath);
        AgentDirectory agentDirectory = classPathResolver.resolve();
        Assert.assertTrue("verify agent directory ", agentDirectory != null);

        String findAgentJar = agentDirectory.getAgentJarName();
        Assert.assertNotNull(findAgentJar);

        String agentJar = agentDirectory.getAgentJarName();
        Assert.assertEquals(BOOTSTRAP_JAR, agentJar);

        String agentPath = agentDirectory.getAgentJarFullPath();
        Assert.assertEquals(agentBootstrapPath, agentPath);

        String agentDirPath = agentDirectory.getAgentDirPath();
        Assert.assertEquals(agentBuildDir, agentDirPath);

        String agentLibPath = agentDirectory.getAgentLibPath();
        Assert.assertEquals(agentBuildDir + File.separator + "lib", agentLibPath);

        List<JarFile> bootstrapJarFile = agentDirectory.getBootDir().openJarFiles();
        closeJarFile(bootstrapJarFile);

    }

    private void closeJarFile(List<JarFile> jarFiles) {
        for (JarFile jarFile : jarFiles) {
            try {
                jarFile.close();
            } catch (IOException e) {
                logger.debug(jarFile + " delete fail", e);
            }
        }
    }

    private static String getClassLocation(Class<?> clazz) {
        URL location = CodeSourceUtils.getCodeLocation(clazz);
        logger.debug("codeSource.getCodeLocation:{}", location);
        File file = FileUtils.toFile(location);
        return file.getPath();
    }


    @Test
    public void findAgentJar() {
        logger.debug("agentBuildDir:{}", agentBuildDir);
        logger.debug("agentBootstrapPath:{}", agentBootstrapPath);

        findAgentJar(agentBootstrapPath);


        findAgentJarAssertFail(agentBuildDir + File.separator + "pinpoint-bootstrap-unknown.jar");
    }

    private void findAgentJar(String path) {
        AgentDirBaseClassPathResolver classPathResolver = new AgentDirBaseClassPathResolver(path);
        String agentJar = classPathResolver.findBootstrapJar(path);
        Assert.assertNotNull(agentJar);
    }

    private void findAgentJarAssertFail(String path) {
        AgentDirBaseClassPathResolver classPathResolver = new AgentDirBaseClassPathResolver(path);
        String agentJar = classPathResolver.findBootstrapJar(path);
        Assert.assertNull(agentJar);
    }

}

