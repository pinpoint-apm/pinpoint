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

import com.navercorp.pinpoint.common.util.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DirectoryScanner implements Scanner {

    private final String directory;

    public DirectoryScanner(String directory) {
        this.directory = Assert.requireNonNull(directory, "directory");
    }

    @Override
    public boolean exist(String fileName) {
        Assert.requireNonNull(fileName, "fileName");

        final String fullPath = getFullPath(fileName);
        final File file = new File(fullPath);

        return file.isFile();
    }

    private String getFullPath(String fileName) {
        return directory + fileName;
    }

    @Override
    public InputStream openStream(String fileName) {
        Assert.requireNonNull(fileName, "fileName");

        final String fullPath = getFullPath(fileName);
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
