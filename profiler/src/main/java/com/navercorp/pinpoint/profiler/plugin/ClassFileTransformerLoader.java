/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.plugin;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.common.util.Assert;

import java.lang.instrument.ClassFileTransformer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ClassFileTransformerLoader {

    private final ProfilerConfig profilerConfig;
    private final DynamicTransformTrigger dynamicTransformTrigger;

    private final List<ClassFileTransformer> classTransformers = new ArrayList<ClassFileTransformer>();

    public ClassFileTransformerLoader(ProfilerConfig profilerConfig, DynamicTransformTrigger dynamicTransformTrigger) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig must not be null");
        this.dynamicTransformTrigger = Assert.requireNonNull(dynamicTransformTrigger, "dynamicTransformTrigger must not be null");
    }

    public void addClassFileTransformer(InstrumentContext instrumentContext, final Matcher matcher, final TransformCallbackProvider transformCallbackProvider) {
        Assert.requireNonNull(instrumentContext, "instrumentContext must not be null");
        Assert.requireNonNull(transformCallbackProvider, "transformCallbackProvider must not be null");

        final MatchableClassFileTransformer guard = new MatchableClassFileTransformerDelegate(profilerConfig, instrumentContext, matcher, transformCallbackProvider);
        classTransformers.add(guard);
    }



    public void addClassFileTransformer(InstrumentContext instrumentContext, ClassLoader classLoader, String targetClassName, TransformCallbackProvider transformCallbackProvider) {
        Assert.requireNonNull(targetClassName, "targetClassName must not be null");
        Assert.requireNonNull(transformCallbackProvider, "transformCallbackProvider must not be null");

        final ClassFileTransformerDelegate classFileTransformerGuardDelegate = new ClassFileTransformerDelegate(profilerConfig, instrumentContext, transformCallbackProvider);

        this.dynamicTransformTrigger.addClassFileTransformer(classLoader, targetClassName, classFileTransformerGuardDelegate);
    }

    public List<ClassFileTransformer> getClassTransformerList() {
        return classTransformers;
    }
}
