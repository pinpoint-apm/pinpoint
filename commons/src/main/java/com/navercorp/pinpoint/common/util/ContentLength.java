package com.navercorp.pinpoint.common.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ContentLength {

    public static final int SKIP = Integer.MIN_VALUE;
    public static final int NOT_EXIST = -1;

    private final LengthFunction[] functions;

    public ContentLength(LengthFunction[] functions) {
        this.functions = Objects.requireNonNull(functions, "functions");
    }

    public int getLength(Object content) {
        long length = getLongLength(content);
        return toInt(length);
    }

    private int toInt(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) value;
    }

    public long getLongLength(Object content) {
        if (content == null) {
            return NOT_EXIST;
        }
        for (LengthFunction function : functions) {
            long length = function.getLength(content);
            if (length == SKIP) {
                continue;
            }
            return length;
        }
        return NOT_EXIST;
    }


    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private static final Map<Class<?>, LengthFunction> MAPPING = getMapping();

        private static Map<Class<?>, LengthFunction> getMapping() {
            Map<Class<?>, LengthFunction> map = new LinkedHashMap<>();
            map.put(String.class, new StringLength());
            map.put(byte[].class, new PrimitiveByteArrayLength());
            map.put(char[].class, new PrimitiveCharArrayLength());
            map.put(File.class, new FileLength());
            map.put(InputStream.class, new InputStreamAvailableLength());
            return map;
        }

        private Builder() {
        }

        private final List<LengthFunction> list = new ArrayList<>();

        public void addContentType(Class<?> content) {
            Objects.requireNonNull(content, "content");
            LengthFunction lengthFunction = MAPPING.get(content);
            if (lengthFunction == null) {
                throw new IllegalArgumentException("unsupported content :" + content);
            }
            list.add(lengthFunction);
        }

        public void addFunction(LengthFunction function) {
            Objects.requireNonNull(function, "function");
            list.add(function);
        }

        public ContentLength build() {
            LengthFunction[] functions = list.toArray(new LengthFunction[0]);
            return new ContentLength(functions);
        }
    }

    public interface LengthFunction {
        long getLength(Object context);
    }

    public static class PrimitiveByteArrayLength implements LengthFunction {
        public long getLength(Object context) {
            if (context instanceof byte[]) {
                return ((byte[]) context).length;
            }
            return SKIP;
        }


    }

    public static class PrimitiveCharArrayLength implements LengthFunction {
        public long getLength(Object context) {
            if (context instanceof char[]) {
                return ((char[]) context).length;
            }
            return SKIP;
        }

    }

    public static class StringLength implements LengthFunction {
        public long getLength(Object context) {
            if (context instanceof String) {
                return ((String) context).length();
            }
            return SKIP;
        }

    }

    public static class FileLength implements LengthFunction {
        public long getLength(Object context) {
            if (context instanceof File) {
                return ((File) context).length();
            }
            return SKIP;
        }
    }

    public static class InputStreamAvailableLength implements LengthFunction {
        public long getLength(Object context) {
            if (context instanceof InputStream) {
                try {
                    return ((InputStream) context).available();
                } catch (IOException ignored) {
                    // io error
                    return NOT_EXIST;
                }
            }
            return SKIP;
        }
    }
}
