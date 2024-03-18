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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DirectoryScanner implements Scanner {

    private final File directory;

    public DirectoryScanner(String directory) {
        Objects.requireNonNull(directory, "directory");
        this.directory = new File(directory);
    }

    @Override
    public boolean exist(String fileName) {
        Objects.requireNonNull(fileName, "fileName");

        final File file = new File(directory, fileName);

        return file.isFile();
    }


    @Override
    public InputStream openStream(String fileName) {
        Objects.requireNonNull(fileName, "fileName");

        final File fullPath = new File(directory, fileName);
        try {
            return new FileInputStream(fullPath);
        } catch (FileNotFoundException e) {
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
