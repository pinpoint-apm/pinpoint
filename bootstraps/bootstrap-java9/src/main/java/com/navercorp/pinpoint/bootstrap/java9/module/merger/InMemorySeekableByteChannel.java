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

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * @author youngjin.kim2
 */
public class InMemorySeekableByteChannel implements SeekableByteChannel {

    private final byte[] bytes;
    private long position = 0;

    public InMemorySeekableByteChannel(byte[] bytes, long position) {
        this(bytes);
        this.position = Math.min(position, bytes.length);
    }

    public InMemorySeekableByteChannel(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public int read(ByteBuffer dst) {
        if (position >= bytes.length) {
            return -1;
        }

        long remain = size() - position;
        int sz = (int) Math.min(remain, dst.capacity());

        dst.put(bytes, (int) position, sz);
        position += sz;
        return sz;
    }

    @Override
    public int write(ByteBuffer src) {
        bytes[(int) position++] = src.get();
        return 1;
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) {
        return new InMemorySeekableByteChannel(bytes, newPosition);
    }

    @Override
    public long size() {
        return bytes.length;
    }

    @Override
    public SeekableByteChannel truncate(long size) {
        throw new RuntimeException("Illegal truncate attempt");
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() {}
}
