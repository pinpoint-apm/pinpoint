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
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.instrument.GuardInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.exception.PinpointException;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author emeroad
 */
public class MatchableClassFileTransformerDelegate implements MatchableClassFileTransformer {

    private final ProfilerConfig profilerConfig;
    private final InstrumentContext instrumentContext;
    private final Matcher matcher;
    private final TransformCallbackProvider transformCallbackProvider;


    public MatchableClassFileTransformerDelegate(ProfilerConfig profilerConfig, InstrumentContext instrumentContext, Matcher matcher, TransformCallbackProvider transformCallbackProvider) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig");
        this.instrumentContext = Assert.requireNonNull(instrumentContext, "instrumentContext");
        this.matcher = Assert.requireNonNull(matcher, "matcher");
        this.transformCallbackProvider = Assert.requireNonNull(transformCallbackProvider, "transformCallback");
    }


    @Override
    public Matcher getMatcher() {
        return matcher;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className == null) {
            throw new NullPointerException("className");
        }

        final InstrumentContext instrumentContext = this.instrumentContext;
        final GuardInstrumentor guard = new GuardInstrumentor(this.profilerConfig, instrumentContext);
        try {
            // WARN external plugin api
            final TransformCallback transformCallback = transformCallbackProvider.getTransformCallback(instrumentContext, loader);
            return transformCallback.doInTransform(guard, loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        } catch (InstrumentException e) {
            throw new PinpointException(e);
        } finally {
            guard.close();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MatchableClassFileTransformerDelegate{");
        sb.append("matcher=").append(matcher);
        sb.append(", transformCallbackProvider=").append(transformCallbackProvider);
        sb.append('}');
        return sb.toString();
    }
}
