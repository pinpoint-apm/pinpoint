package com.navercorp.pinpoint.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Random;

public class IOUtilsTest {

    private final Random random = new Random();

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

    @Test
    public void toByteArray_close_on_every_path() throws IOException {
        // fast path: the stream ends before the buffer is full
        assertClosed(100, 4096);
        // exact fit: the buffer is full and the stream is at EOF
        assertClosed(3000, 3000);
        // slow path: available() underestimates the size
        assertClosed(6000, 1);
    }

    private void assertClosed(final int sourceBytesSize, final int available) throws IOException {
        ByteArrayInputStream inputStream = Mockito.spy(new ByteArrayInputStream(nextBytes(sourceBytesSize)));
        Mockito.doReturn(available).when(inputStream).available();

        IOUtils.toByteArray(inputStream, true);

        Mockito.verify(inputStream).close();
    }

    private void assertToByteArray(final int sourceBytesSize) throws IOException {
        assertToByteArray(sourceBytesSize, sourceBytesSize);
    }

    private void assertToByteArray(final int sourceBytesSize, final int available) throws IOException {
        assertToByteArray(sourceBytesSize, available, Integer.MAX_VALUE);
    }

    private void assertToByteArray(final int sourceBytesSize, final int available, final int osBuffer) throws IOException {
        byte[] source = nextBytes(sourceBytesSize);

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

    private byte[] nextBytes(int size) {
        byte[] bytes = new byte[size];
        random.nextBytes(bytes);
        return bytes;
    }

}
