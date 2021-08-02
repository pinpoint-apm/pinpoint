package com.navercorp.pinpoint.common.util;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;


public class IOUtilsTest {

    @Test
    public void toByteArray_small1() throws IOException {
        byte[] source = RandomUtils.nextBytes(32);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(source);

        byte[] bytes = IOUtils.toByteArray(inputStream);

        org.junit.Assert.assertArrayEquals(source, bytes);
    }

    @Test
    public void toByteArray_small2() throws IOException {
        byte[] source = RandomUtils.nextBytes(1024);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(source);

        byte[] bytes = IOUtils.toByteArray(inputStream);

        org.junit.Assert.assertArrayEquals(source, bytes);
    }

    @Test
    public void toByteArray_large() throws IOException {
        byte[] source = RandomUtils.nextBytes(4096 * 4);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(source);

        byte[] bytes = IOUtils.toByteArray(inputStream);

        org.junit.Assert.assertArrayEquals(source, bytes);
    }

    @Test
    public void toByteArray_available_return0() throws IOException {
        byte[] source = RandomUtils.nextBytes(4096 * 2);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(source) {
            @Override
            public synchronized int available() {
                return 0;
            }
        };

        byte[] bytes = IOUtils.toByteArray(inputStream);

        org.junit.Assert.assertArrayEquals(source, bytes);
    }

}
