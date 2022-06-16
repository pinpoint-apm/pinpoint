/*
 * Copyright 2022 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.bootstrap.java9.module.merger;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;

/**
 * @author youngjin.kim2
 */
class SingleFileSystemPath implements Path {

    private final SingleFileSystem fs;
    private final String name;

    SingleFileSystemPath(SingleFileSystem fs, String name) {
        this.fs = fs;
        this.name = name;
    }

    @Override
    public FileSystem getFileSystem() {
        return fs;
    }

    @Override
    public boolean isAbsolute() {
        return true;
    }

    @Override
    public Path getRoot() {
        return fs.getPath(null);
    }

    @Override
    public Path getFileName() {
        return fs.getPath(null);
    }

    @Override
    public Path getParent() {
        return fs.getPath(null);
    }

    @Override
    public int getNameCount() {
        return 1;
    }

    @Override
    public Path getName(int index) {
        return fs.getPath(null);
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        return fs.getPath(null);
    }

    @Override
    public boolean startsWith(Path other) {
        return true;
    }

    @Override
    public boolean endsWith(Path other) {
        return true;
    }

    @Override
    public Path normalize() {
        return fs.getPath(null);
    }

    @Override
    public Path resolve(Path other) {
        return fs.getPath(null);
    }

    @Override
    public Path relativize(Path other) {
        return fs.getPath(null);
    }

    @Override
    public URI toUri() {
        return null;
    }

    @Override
    public Path toAbsolutePath() {
        return fs.getPath(null);
    }

    @Override
    public Path toRealPath(LinkOption... options) {
        return fs.getPath(null);
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) {
        throw new RuntimeException("SingleFileSystemPath::register is called");
    }

    @Override
    public int compareTo(Path other) {
        return 0;
    }

    @Override
    public String toString() {
        return name;
    }
}
