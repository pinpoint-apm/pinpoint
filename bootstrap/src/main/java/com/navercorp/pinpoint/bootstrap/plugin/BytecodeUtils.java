package com.nhn.pinpoint.bootstrap.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public abstract class BytecodeUtils {
    private BytecodeUtils() { }

    private static final Method DEFINE_CLASS;
    
    static {
        Method method = null;
        
        try {
            method = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        DEFINE_CLASS = method;
    }

    public static Class<?> defineClass(ClassLoader classLoader, String className, byte[] classFile) {
        try {
            return (Class<?>)DEFINE_CLASS.invoke(classLoader, className, classFile, 0, classFile.length);
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] getClassFile(ClassLoader classLoader, String className) {
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        if (className == null) {
            throw new NullPointerException("className must not be null");
        }

        final InputStream is = classLoader.getResourceAsStream(className.replace('.', '/') + ".class");
        if (is == null) {
            throw new RuntimeException("No such class file: " + className);
        }
        
        ReadableByteChannel channel = Channels.newChannel(is);
        ByteBuffer buffer;
        
        try {
            buffer = ByteBuffer.allocate(is.available());
        
            while (channel.read(buffer) >= 0) {
                if (buffer.remaining() == 0) {
                    buffer.flip();
                    ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                    newBuffer.put(buffer);
                    buffer = newBuffer;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            close(is);
        }
        
        return buffer.array();
    }

    private static void close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                // skip
            }
        }
    }
}
