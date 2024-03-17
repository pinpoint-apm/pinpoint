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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarFile;

/**
 * @author emeroad
 */
public class AgentDirBaseClassPathResolverTest {

    private static final Logger logger = LogManager.getLogger(AgentDirBaseClassPathResolverTest.class);

    private static final Path BOOTSTRAP_JAR = Paths.get("pinpoint-bootstrap-" + Version.VERSION + ".jar");
    private static final String TEST_AGENT_DIR = "testagent";

    @TempDir
    static Path TEMP_DIR;

    private static final AtomicInteger AGENT_ID_ALLOCATOR = new AtomicInteger();

    private static Path agentBuildDir;
    private static Path agentBootstrapPath;

    private static AgentDirGenerator agentDirGenerator;

    @BeforeAll
    public static void beforeClass() throws Exception {

        TEMP_DIR = TEMP_DIR.toRealPath().normalize();
        logger.debug("buildDir:{}", TEMP_DIR);

        String testDir = TEST_AGENT_DIR + '_' + AGENT_ID_ALLOCATOR.incrementAndGet();
        agentBuildDir = TEMP_DIR.resolve(testDir);

        logger.debug("agentBuildDir:{}", agentBuildDir);

        agentBootstrapPath = agentBuildDir.resolve(BOOTSTRAP_JAR);

        logger.debug("agentBootstrapPath:{}", agentBootstrapPath);

        createAgentDir(agentBuildDir);


    }

    private static void createAgentDir(Path tempAgentDir) throws IOException {
        agentDirGenerator = new AgentDirGenerator(tempAgentDir, Version.VERSION);
        agentDirGenerator.create();
    }


    @Test
    public void testFindAgentJar() {

        logger.debug("TEST_AGENT_DIR:{}", agentBuildDir);
        logger.debug("agentBootstrapPath:{}", agentBootstrapPath);

        AgentDirBaseClassPathResolver classPathResolver = new AgentDirBaseClassPathResolver(agentBootstrapPath);
        AgentDirectory agentDirectory = classPathResolver.resolve();
        Assertions.assertNotNull(agentDirectory, "verify agent directory ");

        Path findAgentJar = agentDirectory.getAgentJarName();
        Assertions.assertNotNull(findAgentJar);

        Path agentJar = agentDirectory.getAgentJarName();
        Assertions.assertEquals(BOOTSTRAP_JAR, agentJar);

        Path agentPath = agentDirectory.getAgentJarFullPath();
        Assertions.assertEquals(agentBootstrapPath, agentPath);

        Path agentDirPath = agentDirectory.getAgentDirPath();
        Assertions.assertEquals(agentBuildDir, agentDirPath);

        Path agentLibPath = agentDirectory.getAgentLibPath();
        Assertions.assertEquals(agentBuildDir.resolve("lib"), agentLibPath);

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
        Assertions.assertNotNull(agentJar);
    }

    private void findAgentJarAssertFail(Path path) {
        AgentDirBaseClassPathResolver classPathResolver = new AgentDirBaseClassPathResolver(path);
        Path agentJar = classPathResolver.findBootstrapJar(path);
        Assertions.assertNull(agentJar);
    }

}

