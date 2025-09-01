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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DirectoryScanner implements Scanner {

    private final Path directory;

    public DirectoryScanner(URL url) throws URISyntaxException {
        Objects.requireNonNull(url, "directory");
        this.directory = Paths.get(url.toURI());
    }

    @Override
    public boolean exist(String fileName) {
        Objects.requireNonNull(fileName, "fileName");

        final Path file = directory.resolve(fileName);

        return Files.isRegularFile(file);
    }


    @Override
    public InputStream openStream(String fileName) {
        Objects.requireNonNull(fileName, "fileName");

        final Path fullPath = directory.resolve(fileName);
        try {
            return Files.newInputStream(fullPath);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void close() {

    }

    @Override
    public String toString() {
        return "DirectoryScanner{" +
                "directory='" + directory + '\'' +
                '}';
    }
}
