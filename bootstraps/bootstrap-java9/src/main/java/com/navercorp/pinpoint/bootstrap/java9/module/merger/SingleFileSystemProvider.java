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

import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author youngjin.kim2
 */
public class SingleFileSystemProvider extends FileSystemProvider {

    private final byte[] bytes;
    private final SingleFileSystem fs;

    SingleFileSystemProvider(String filename, byte[] bytes) {
        this.bytes = bytes;
        this.fs = new SingleFileSystem(this, filename);
    }

    @Override
    public String getScheme() {
        return "sfs";
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) {
        return fs;
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        return fs;
    }

    @Override
    public Path getPath(URI uri) {
        return fs.getPath(null);
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) {
        return new InMemorySeekableByteChannel(bytes);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) {
        return new DirectoryStream<>() {
            @Override
            public Iterator<Path> iterator() {
                return Collections.emptyIterator();
            }

            @Override
            public void close() {}
        };
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) {}

    @Override
    public void delete(Path path) {}

    @Override
    public void copy(Path source, Path target, CopyOption... options) {}

    @Override
    public void move(Path source, Path target, CopyOption... options) {}

    @Override
    public boolean isSameFile(Path path, Path path2) {
        return true;
    }

    @Override
    public boolean isHidden(Path path) {
        return false;
    }

    @Override
    public FileStore getFileStore(Path path) {
        return null;
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) {}

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        return null;
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) {
        final BasicFileAttributes attrs = new SimpleFileAttribute(bytes.length);
        if (type.isInstance(attrs)) {
            return type.cast(attrs);
        }
        return null;
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) {
        return null;
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) {

    }

    private static class SimpleFileAttribute implements BasicFileAttributes {

        private final long size;

        public SimpleFileAttribute(long size) {
            this.size = size;
        }

        @Override
        public FileTime lastModifiedTime() {
            return null;
        }

        @Override
        public FileTime lastAccessTime() {
            return null;
        }

        @Override
        public FileTime creationTime() {
            return null;
        }

        @Override
        public boolean isRegularFile() {
            return true;
        }

        @Override
        public boolean isDirectory() {
            return false;
        }

        @Override
        public boolean isSymbolicLink() {
            return false;
        }

        @Override
        public boolean isOther() {
            return false;
        }

        @Override
        public long size() {
            return size;
        }

        @Override
        public Object fileKey() {
            return null;
        }
    }
}
