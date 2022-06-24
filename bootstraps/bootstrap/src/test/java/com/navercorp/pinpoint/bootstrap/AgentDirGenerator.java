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

import com.navercorp.pinpoint.common.Version;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentDirGenerator {

    private final Logger logger = LogManager.getLogger(this.getClass());


    private static final Path bootStrapJar = Paths.get("pinpoint-bootstrap-" + Version.VERSION + ".jar");

    private static final Path commons = Paths.get("pinpoint-commons-" + Version.VERSION + ".jar");
    private static final Path bootStrapCoreJar = Paths.get("pinpoint-bootstrap-core-" + Version.VERSION + ".jar");
    private static final Path bootStrapJava9Jar = Paths.get("pinpoint-bootstrap-java9-" + Version.VERSION + ".jar");
    private static final Path bootStrapCoreOptionalJar = Paths.get("pinpoint-java7-" + Version.VERSION + ".jar");
    private static final Path annotations = Paths.get("pinpoint-annotations-" + Version.VERSION + ".jar");

    private final Path agentDirPath;

    public AgentDirGenerator(Path agentDirPath) {
        this.agentDirPath = Objects.requireNonNull(agentDirPath, "agentDirPath");
    }

    public void create() throws IOException {

        createDir(agentDirPath);

        // create dummy bootstrap
        createJarFile(agentDirPath, bootStrapJar);

        Path boot = createChildDir(agentDirPath, "boot");

        createJarFile(boot, commons);
        createJarFile(boot, bootStrapCoreJar);
        createJarFile(boot, bootStrapJava9Jar);
        createJarFile(boot, bootStrapCoreOptionalJar);
        createJarFile(boot, annotations);
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


    private void createFile(Path parentDir, Path filepath) throws IOException {
        logger.debug("create file : {}/{}",  parentDir, filepath);

        final Path file = parentDir.resolve(filepath);
        Files.createFile(file);

    }

    private void createJarFile(Path parentDir, Path filepath) throws IOException {
        final Path jarPath = parentDir.resolve(filepath);
        logger.debug("create jar:{}", jarPath);

        Manifest manifest = new Manifest();
        try (OutputStream out = Files.newOutputStream(jarPath);
             JarOutputStream jos = new JarOutputStream(out, manifest)){
        }

    }

    public void remove() throws IOException {
        File file = agentDirPath.toFile();
        deleteDirectory(file);
    }

    // https://www.baeldung.com/java-delete-directory
    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
