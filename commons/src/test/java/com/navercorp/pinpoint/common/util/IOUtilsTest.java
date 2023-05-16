package com.navercorp.pinpoint.common.util;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class IOUtilsTest {

    @Test
    public void toByteArray_small() throws IOException {
        assertToByteArray(0);
        assertToByteArray(1);
        assertToByteArray(32);
        assertToByteArray(256);
        assertToByteArray(512);
        assertToByteArray(1024);
    }


    @Test
    public void toByteArray_large() throws IOException {
        assertToByteArray(IOUtils.DEFAULT_BUFFER_SIZE - 1);
        assertToByteArray(IOUtils.DEFAULT_BUFFER_SIZE);
        assertToByteArray(IOUtils.DEFAULT_BUFFER_SIZE + 1);
        assertToByteArray(IOUtils.DEFAULT_BUFFER_SIZE * 4);
        assertToByteArray(IOUtils.DEFAULT_BUFFER_SIZE * 8);
    }

    @Test
    public void toByteArray_large2() throws IOException {
//        assertByteReadTest(IOUtils.DEFAULT_BUFFER_SIZE - 1);
//        assertByteReadTest(IOUtils.DEFAULT_BUFFER_SIZE);
//        assertByteReadTest(IOUtils.DEFAULT_BUFFER_SIZE + 1);
        assertToByteArray(IOUtils.DEFAULT_BUFFER_SIZE * 4);
//        assertByteReadTest(IOUtils.DEFAULT_BUFFER_SIZE * 4);
    }

    @Test
    public void toByteArray_available_unsupported() throws IOException {
        assertToByteArray(IOUtils.DEFAULT_BUFFER_SIZE * 2, 0);
        assertToByteArray(IOUtils.DEFAULT_BUFFER_SIZE * 2, 1);
    }

    @Test
    public void toByteArray_available_return4096() throws IOException {
        assertToByteArray(IOUtils.DEFAULT_BUFFER_SIZE + 1, 4096);
        assertToByteArray(IOUtils.DEFAULT_BUFFER_SIZE, 4096);
        assertToByteArray(IOUtils.DEFAULT_BUFFER_SIZE + 1, 4096);
        assertToByteArray(IOUtils.DEFAULT_BUFFER_SIZE * 2, 4096);
        assertToByteArray(IOUtils.DEFAULT_BUFFER_SIZE * 5, 4096);
        assertToByteArray(IOUtils.DEFAULT_BUFFER_SIZE * 10, 4096);
    }

    @Test
    public void toByteArray_available_os_buffer_small() throws IOException {
        assertToByteArray(IOUtils.DEFAULT_BUFFER_SIZE + 1, 4096, 1024);
        assertToByteArray(IOUtils.DEFAULT_BUFFER_SIZE, 4096);
        assertToByteArray(IOUtils.DEFAULT_BUFFER_SIZE + 1, 4096, 1024);
        assertToByteArray(IOUtils.DEFAULT_BUFFER_SIZE * 2, 4096, 1024);
        assertToByteArray(IOUtils.DEFAULT_BUFFER_SIZE * 5, 4096, 1024);
        assertToByteArray(IOUtils.DEFAULT_BUFFER_SIZE * 10, 4096, 1024);
    }

    private void assertToByteArray(final int sourceBytesSize) throws IOException {
        assertToByteArray(sourceBytesSize, sourceBytesSize);
    }

    private void assertToByteArray(final int sourceBytesSize, final int available) throws IOException {
        assertToByteArray(sourceBytesSize, available, Integer.MAX_VALUE);
    }

    private void assertToByteArray(final int sourceBytesSize, final int available, final int osBuffer) throws IOException {
        byte[] source = RandomUtils.nextBytes(sourceBytesSize);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(source) {
            @Override
            public synchronized int available() {
                return available;
            }

            @Override
            public synchronized int read(byte[] b, int off, int len) {
                // os buffer emulation
                int min = Math.min(len, osBuffer);
                return super.read(b, off, min);
            }
        };

        byte[] bytes = IOUtils.toByteArray(inputStream);

        Assertions.assertArrayEquals(source, bytes);
    }

}
