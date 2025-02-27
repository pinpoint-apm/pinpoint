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

package com.navercorp.pinpoint.profiler.instrument.scanner;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JarFileRepository {
    private final JarFileScanner[] scanners;


    public JarFileRepository(List<Path> jarFilePathList) {
        this.scanners = newJarScanner(jarFilePathList);
    }

    private JarFileScanner[] newJarScanner(List<Path> jarFilePathList) {
        Objects.requireNonNull(jarFilePathList, "jarFilePathList");

        final List<JarFileScanner> jarFileList = new ArrayList<>(jarFilePathList.size());
        for (Path jarFilePath : jarFilePathList) {
            JarFileScanner jarFileScanner = JarFileScanner.of(jarFilePath);
            jarFileList.add(jarFileScanner);
        }
        return jarFileList.toArray(new JarFileScanner[0]);
    }


    public InputStream openStream(String resourceName) {
        Objects.requireNonNull(resourceName, "resourceName");

        for (Scanner scanner : scanners) {
            final InputStream inputStream = scanner.openStream(resourceName);
            if (inputStream != null) {
                return inputStream;
            }
        }
        return null;
    }

    public void close() {
        for (Scanner scanner : scanners) {
            scanner.close();
        }
    }


}
