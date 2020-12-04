package com.navercorp.pinpoint.plugin.grpc;

import io.grpc.ManagedChannelBuilder;

import java.lang.reflect.Method;

public class BuilderUtils {

    private interface Call<T> {
        void call(T target);
    }

    private static Call<ManagedChannelBuilder<?>> usePlaintext = getUsePlainText();

    public static void usePlainText(ManagedChannelBuilder<?> builder) {
        usePlaintext.call(builder);
    }

    private static Call getUsePlainText() {
        final Class<ManagedChannelBuilder> builderClass = ManagedChannelBuilder.class;

        final Method oldUsePlaintext = getMethod(builderClass, "usePlaintext");
        if (oldUsePlaintext != null) {
            return new Call<ManagedChannelBuilder<?>>() {
                @Override
                public void call(ManagedChannelBuilder<?> target) {
                    try {
                        oldUsePlaintext.invoke(target);
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
            };
        }

        final Method newUsePlaintext = getMethod(builderClass, "usePlaintext", boolean.class);
        if (newUsePlaintext != null) {
            return new Call<ManagedChannelBuilder<?>>() {
                @Override
                public void call(ManagedChannelBuilder<?> target) {
                    try {
                        newUsePlaintext.invoke(target, true);
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
            };
        }

        throw new UnsupportedOperationException("usePlaintext method not found");
    }

    private static Method getMethod(Class<ManagedChannelBuilder> builderClass, String method, Class<?>... parameterTypes) {
        try {
            return builderClass.getMethod(method, parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}