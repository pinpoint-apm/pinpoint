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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.jar.JarFile;

/**
 * @author Woonduk Kang(emeroad)
 */
final class JarFileUtils {

    public static JarFile openJarFile(Path path) {
        Objects.requireNonNull(path, "path");

        if (!Files.exists(path)) {
            throw new IllegalArgumentException(path + " not found");
        }
        if (Files.isDirectory(path)) {
            throw new IllegalArgumentException(path + " is directory");
        }
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException(path + " not file");
        }
        if (!Files.isReadable(path)) {
            throw new IllegalArgumentException(path + " can read");
        }
        try {
            return new JarFile(path.toFile());
        } catch (IOException e) {
            throw new IllegalStateException(path + " create fail Caused by:" + e.getMessage(), e);
        }
    }
}
