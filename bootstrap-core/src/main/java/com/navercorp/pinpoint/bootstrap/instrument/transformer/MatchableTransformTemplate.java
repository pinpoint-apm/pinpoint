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
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.common.annotations.InterfaceStability;
import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author jaehong.kim
 */
@InterfaceStability.Unstable
public class MatchableTransformTemplate implements TransformOperations {
    private final InstrumentContext instrumentContext;

    public MatchableTransformTemplate(InstrumentContext instrumentContext) {
        if (instrumentContext == null) {
            throw new NullPointerException("instrumentContext must not be null");
        }
        this.instrumentContext = instrumentContext;
    }

    @Override
    public void transform(String className, TransformCallback transformCallback) {
        Assert.requireNonNull(className, "className must not be null");
        Assert.requireNonNull(transformCallback, "transformCallback must not be null");
        final Matcher matcher = Matchers.newClassNameMatcher(className);
        this.instrumentContext.addClassFileTransformer(matcher, transformCallback);
    }

    public void transform(final Matcher matcher, TransformCallback transformCallback) {
        Assert.requireNonNull(matcher, "matcher must not be null");
        Assert.requireNonNull(transformCallback, "transformCallback must not be null");
        this.instrumentContext.addClassFileTransformer(matcher, transformCallback);
    }
}