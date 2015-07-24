package com.navercorp.pinpoint.plugin.google.httpclient;

import java.lang.reflect.Modifier;

import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;

public class HttpRequestExceuteAsyncMethodInnerClassMethodFilter implements MethodFilter {
    private static final int SYNTHETIC = 0x00001000;

    @Override
    public boolean accept(MethodInfo method) {
        final int modifiers = method.getModifiers();

        if (isSynthetic(modifiers) || !Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || Modifier.isAbstract(modifiers) || Modifier.isNative(modifiers)) {
            return REJECT;
        }

        final String name = method.getName();
        if (name.equals("call")) {
            return ACCEPT;
        }

        return REJECT;
    }

    private boolean isSynthetic(int mod) {
        return (mod & SYNTHETIC) != 0;
    }
}