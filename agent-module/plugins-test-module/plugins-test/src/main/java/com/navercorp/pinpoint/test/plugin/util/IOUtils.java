package com.navercorp.pinpoint.test.plugin.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public final class IOUtils {

    /**
     * @param inputStream streams are closed by the caller
     */
    public static byte[] toByteArray(final InputStream inputStream) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    public static byte[] toByteArray(final URL url) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            return toByteArray(inputStream);
        }
    }
}
