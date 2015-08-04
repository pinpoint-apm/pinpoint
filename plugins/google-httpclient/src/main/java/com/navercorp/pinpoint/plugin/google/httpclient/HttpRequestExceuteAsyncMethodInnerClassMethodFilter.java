/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.google.httpclient;

import java.lang.reflect.Modifier;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;

/**
 * 
 * @author jaehong.kim
 *
 */
public class HttpRequestExceuteAsyncMethodInnerClassMethodFilter implements MethodFilter {
    private static final int SYNTHETIC = 0x00001000;

    @Override
    public boolean accept(InstrumentMethod method) {
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