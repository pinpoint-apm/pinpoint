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

package com.navercorp.pinpoint.profiler.plugin;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.instrument.GuardInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.exception.PinpointException;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author emeroad
 */
public class ClassFileTransformerGuardDelegate implements ClassFileTransformer {

    private final ProfilerConfig profilerConfig;
    private final InstrumentContext instrumentContext;
    private final TransformCallback transformCallback;

    public ClassFileTransformerGuardDelegate(ProfilerConfig profilerConfig, InstrumentContext instrumentContext, TransformCallback transformCallback) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (instrumentContext == null) {
            throw new NullPointerException("instrumentContext must not be null");
        }
        if (transformCallback == null) {
            throw new NullPointerException("transformCallback must not be null");
        }
        this.profilerConfig = profilerConfig;
        this.instrumentContext = instrumentContext;
        this.transformCallback = transformCallback;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className == null) {
            throw new NullPointerException("className must not be null");
        }

        final GuardInstrumentor guard = new GuardInstrumentor(this.profilerConfig, this.instrumentContext);
        try {
            // WARN external plugin api
            return transformCallback.doInTransform(guard, loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        } catch (InstrumentException e) {
            throw new PinpointException(e);
        } finally {
            guard.close();
        }
    }
}
