package com.nhn.pinpoint.common.util;

import java.io.*;
import java.util.Properties;

/**
 * @author emeroad
 */
public class PropertyUtils {
    public static final String DEFAULT_ENCODING = "UTF-8";

    public static interface InputStreamFactory {
        InputStream openInputStream() throws IOException;
    }

    public static Properties loadProperty(final String filePath) throws IOException {
        if (filePath == null) {
            throw new NullPointerException("filePath must not be null");
        }
        final InputStreamFactory inputStreamFactory = new InputStreamFactory() {
            @Override
            public InputStream openInputStream() throws IOException {
                return new FileInputStream(filePath);
            }
        };
        return loadProperty(new Properties(), inputStreamFactory, DEFAULT_ENCODING);
	}

    public static Properties loadPropertyFromClassPath(final String classPath) throws IOException {
        if (classPath == null) {
            throw new NullPointerException("classPath must not be null");
        }
        final InputStreamFactory inputStreamFactory = new InputStreamFactory() {
            @Override
            public InputStream openInputStream() throws IOException {
                return ClassLoaderUtils.getDefaultClassLoader(PropertyUtils.class.getClassLoader()).getResourceAsStream(classPath);
            }
        };
        return loadProperty(new Properties(), inputStreamFactory, DEFAULT_ENCODING);
    }


    public static Properties loadProperty(Properties properties, InputStreamFactory inputStreamFactory, String encoding) throws IOException {
        if (properties == null) {
            throw new NullPointerException("properties must not be null");
        }
        if (inputStreamFactory == null) {
            throw new NullPointerException("inputStreamFactory must not be null");
        }
        if (encoding == null) {
            throw new NullPointerException("encoding must not be null");
        }
        InputStream in = null;
        Reader reader = null;
        try {
            in = inputStreamFactory.openInputStream();
            reader = new InputStreamReader(in, encoding);
            properties.load(reader);
        } finally {
            close(reader);
            close(in);
        }
        return properties;
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignore) {
            }
        }
    }

}
