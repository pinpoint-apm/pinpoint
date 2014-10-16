package com.nhn.pinpoint.profiler.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import com.nhn.pinpoint.profiler.modifier.Modifier;

public class ClassTransformHelper {
    private static final Method DEFINE_CLASS;
    
    static {
        Method method = null;
        
        try {
            method = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        
        method.setAccessible(true);
        DEFINE_CLASS = method;
    }

    public static Class<?> defineClass(ClassLoader classLoader, String className, byte[] classFile) {
        try {
            return (Class<?>)DEFINE_CLASS.invoke(classLoader, className, classFile, 0, classFile.length);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static Class<?> transformClass(ClassLoader classLoader, String className, Modifier modifier) {
        byte[] original = getClassFile(classLoader, className);
        byte[] transformed = modifier.modify(classLoader, className, null, original);
        
        return defineClass(classLoader, className, transformed);
    }

    public static byte[] getClassFile(ClassLoader classLoader, String className) {
        InputStream is = classLoader.getResourceAsStream(className.replace('.', '/') + ".class");
        
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
        }
        
        return buffer.array();
    }
}
