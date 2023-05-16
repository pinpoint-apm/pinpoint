/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap;

import com.navercorp.pinpoint.bootstrap.agentdir.AgentDirBaseClassPathResolver;
import com.navercorp.pinpoint.bootstrap.agentdir.JarDescription;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentDirGenerator {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Path bootStrapJar;
    private final Path agentDirPath;
    private final String version;

    public AgentDirGenerator(Path agentDirPath, String version) {
        this.agentDirPath = Objects.requireNonNull(agentDirPath, "agentDirPath");
        this.version = Objects.requireNonNull(version, "version");

        this.bootStrapJar = libPath("pinpoint-bootstrap", version);
    }

    private static Path libPath(String name, String version) {
        return Paths.get(name + "-" + version + ".jar");
    }

    public void create() throws IOException {

        createDir(agentDirPath);

        // create dummy bootstrap
        createJarFile(agentDirPath, bootStrapJar);

        Path boot = createChildDir(agentDirPath, "boot");

        AgentDirBaseClassPathResolver bootDir = new AgentDirBaseClassPathResolver(boot);
        List<JarDescription> bootJarDescriptions = bootDir.getBootJarDescriptions();
        for (JarDescription description : bootJarDescriptions) {
            createJarFile(boot, Paths.get(description.getJarName(version)));
        }
    }

    private Path createChildDir(Path agentDir, String childDir) {
        Path childDirPath = agentDir.toAbsolutePath().resolve(childDir);
        return createDir(childDirPath);
    }

    private Path createDir(Path dirPath) {
        logger.debug("create dir:{}", dirPath);

        if (!dirPath.toFile().exists()) {

            try {
                Files.createDirectory(dirPath);
            } catch (IOException e) {
                throw new RuntimeException(dirPath + " create fil", e);
            }
        }
        Assertions.assertTrue(Files.isDirectory(dirPath), dirPath + " not a directory");

        Assertions.assertTrue(Files.isWritable(dirPath));

        return dirPath;
    }


    private void createJarFile(Path parentDir, Path filepath) throws IOException {
        final Path jarPath = parentDir.resolve(filepath);
        logger.info("create jar:{}", jarPath);

        Manifest manifest = new Manifest();
        try (OutputStream out = Files.newOutputStream(jarPath);
             JarOutputStream jos = new JarOutputStream(out, manifest)){
        }

    }

}
