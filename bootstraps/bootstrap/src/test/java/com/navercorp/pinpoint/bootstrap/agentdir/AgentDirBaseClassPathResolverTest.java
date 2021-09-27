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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarFile;

/**
 * @author emeroad
 */
public class AgentDirBaseClassPathResolverTest {

    private static final Logger logger = LoggerFactory.getLogger(AgentDirBaseClassPathResolverTest.class);

    private static final Path BOOTSTRAP_JAR = Paths.get("pinpoint-bootstrap-" + Version.VERSION + ".jar");
    private static final String TEST_AGENT_DIR = "testagent";

    private static final AtomicInteger AGENT_ID_ALLOCATOR = new AtomicInteger();

    private static Path agentBuildDir;
    private static Path agentBootstrapPath;

    private static AgentDirGenerator agentDirGenerator;

    @BeforeClass
    public static void beforeClass() throws Exception {

        Path classLocation = getClassLocation(AgentDirBaseClassPathResolverTest.class);
        logger.debug("buildDir:{}", classLocation);

        String testDir = TEST_AGENT_DIR + '_' + AGENT_ID_ALLOCATOR.incrementAndGet();;
        agentBuildDir = classLocation.resolve(testDir);

        logger.debug("agentBuildDir:{}", agentBuildDir);

        agentBootstrapPath = agentBuildDir.resolve(BOOTSTRAP_JAR);

        logger.debug("agentBootstrapPath:{}", agentBootstrapPath);

        createAgentDir(agentBuildDir);


    }

    private static void createAgentDir(Path tempAgentDir) throws IOException {

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

        Path findAgentJar = agentDirectory.getAgentJarName();
        Assert.assertNotNull(findAgentJar);

        Path agentJar = agentDirectory.getAgentJarName();
        Assert.assertEquals(BOOTSTRAP_JAR, agentJar);

        Path agentPath = agentDirectory.getAgentJarFullPath();
        Assert.assertEquals(agentBootstrapPath, agentPath);

        Path agentDirPath = agentDirectory.getAgentDirPath();
        Assert.assertEquals(agentBuildDir, agentDirPath);

        Path agentLibPath = agentDirectory.getAgentLibPath();
        Assert.assertEquals(agentBuildDir.resolve("lib"), agentLibPath);

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

    private static Path getClassLocation(Class<?> clazz) throws Exception {
        URL location = CodeSourceUtils.getCodeLocation(clazz);
        logger.debug("codeSource.getCodeLocation:{}", location);
        return Paths.get(location.toURI());
    }


    @Test
    public void findAgentJar() {
        logger.debug("agentBuildDir:{}", agentBuildDir);
        logger.debug("agentBootstrapPath:{}", agentBootstrapPath);

        findAgentJar(agentBootstrapPath);


        findAgentJarAssertFail(Paths.get(agentBuildDir.toString(), "pinpoint-bootstrap-unknown.jar"));
    }

    private void findAgentJar(Path path) {
        AgentDirBaseClassPathResolver classPathResolver = new AgentDirBaseClassPathResolver(path);
        Path agentJar = classPathResolver.findBootstrapJar(path);
        Assert.assertNotNull(agentJar);
    }

    private void findAgentJarAssertFail(Path path) {
        AgentDirBaseClassPathResolver classPathResolver = new AgentDirBaseClassPathResolver(path);
        Path agentJar = classPathResolver.findBootstrapJar(path);
        Assert.assertNull(agentJar);
    }

}

