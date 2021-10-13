/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.bootstrap.instrument.transformer;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.TransformMatcherMetadata;
import com.navercorp.pinpoint.common.annotations.InterfaceStability;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
@InterfaceStability.Unstable
public class MatchableTransformTemplate extends TransformTemplate {
    private final TransformMatcherMetadata transformMatcherMetadata;

    public MatchableTransformTemplate(InstrumentContext instrumentContext, TransformMatcherMetadata transformMatcherMetadata) {
        super(instrumentContext);
        this.transformMatcherMetadata = transformMatcherMetadata;
    }

    public void transform(final Matcher matcher, TransformCallback transformCallback) {
        Objects.requireNonNull(matcher, "matcher");
        Objects.requireNonNull(transformCallback, "transformCallback");
        final InstrumentContext instrumentContext = getInstrumentContext();
        instrumentContext.addClassFileTransformer(matcher, transformCallback);
    }

    public void transform(final Matcher matcher, Class<? extends TransformCallback> transformCallbackClass) {
        Objects.requireNonNull(matcher, "matcher");
        Objects.requireNonNull(transformCallbackClass, "transformCallbackClass");

        TransformCallbackChecker.validate(transformCallbackClass);

        // release class reference
        final String transformCallbackName = transformCallbackClass.getName();
        final InstrumentContext instrumentContext = getInstrumentContext();
        instrumentContext.addClassFileTransformer(matcher, transformCallbackName);
    }

    public TransformMatcherMetadata getTransformMatcherMetadata() {
        return this.transformMatcherMetadata;
    }
}