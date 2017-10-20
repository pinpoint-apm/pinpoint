/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.thrift.io;

import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Not Thread Safe.
 *
 * @author Taejin Koo
 */
public class ByteBufferOutputStream extends OutputStream implements ResettableOutputStream {

    private final ByteBuffer byteBuffer;

    public ByteBufferOutputStream(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    @Override
    public void write(int b) throws IOException {
        checkWriteAvailable(1);
        byteBuffer.put((byte) b);
    }

    @Override
    public void write(byte[] byteArray, int offset, int length) throws IOException {
        checkWriteAvailable(length);
        byteBuffer.put(byteArray, offset, length);
    }

    private void checkWriteAvailable(int size) {
        if (byteBuffer.remaining() >= size) {
            return;
        }

        byteBuffer.limit(byteBuffer.capacity());
        if (byteBuffer.remaining() < size) {
            throw new BufferOverflowException("write failed remaining-size:" + byteBuffer.remaining() + ", input-size:" + size + ".");
        }
    }

    @Override
    public void flush() throws IOException {
        // ignore
    }

    @Override
    public void close() throws IOException {
        deallocate(byteBuffer);
    }

    private void deallocate(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return;
        }

        if (byteBuffer instanceof DirectBuffer) {
            Cleaner cleaner = ((DirectBuffer) byteBuffer).cleaner();
            if (cleaner != null) {
                cleaner.clean();
            }
        }
    }


    public ByteBuffer getByteBuffer() {
        byteBuffer.flip();
        return byteBuffer;
    }

    public void clear() {
        byteBuffer.clear();
    }

    @Override
    public void mark() {
        byteBuffer.mark();
    }

    @Override
    public void resetToMarkIndex() {
        byteBuffer.reset();
    }

}
