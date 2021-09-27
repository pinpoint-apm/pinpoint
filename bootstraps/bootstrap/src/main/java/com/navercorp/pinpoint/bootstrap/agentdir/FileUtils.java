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

import com.navercorp.pinpoint.bootstrap.BootLogger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * @author Woonduk Kang(emeroad)
 */
final class FileUtils {

    private static final BootLogger logger = BootLogger.getLogger(FileUtils.class);

    private FileUtils() {
    }

    public static List<Path> listFiles(Path dirPath, String glob) {
        Objects.requireNonNull(dirPath, "dirPath");
        Objects.requireNonNull(glob, "glob");

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(dirPath, glob)) {
            List<Path> list = new ArrayList<>();
            for (Path path : paths) {
                list.add(path);
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException(dirPath + " Path IO error" , e);
        }
    }


    public static Path toRealPath(Path path) {
        try {
            return path.toRealPath().normalize();
        } catch (IOException e) {
            logger.warn(path + " toRealPath() error. Error:" + e.getMessage(), e);
            return path.toAbsolutePath();
        }
    }

}
