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

package com.navercorp.pinpoint.bootstrap.instrument.transformer;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;

/**
 * @author emeroad
 */
public class TransformTemplate implements TransformOperations {

    private final InstrumentContext instrumentContext;

    public TransformTemplate(InstrumentContext instrumentContext) {
        if (instrumentContext == null) {
            throw new NullPointerException("instrumentContext must not be null");
        }
        this.instrumentContext = instrumentContext;
    }

    @Override
    public void transform(String className, TransformCallback transformCallback) {
        if (className == null) {
            throw new NullPointerException("className must not be null");
        }
        if (transformCallback == null) {
            throw new NullPointerException("transformCallback must not be null");
        }
        final Matcher matcher = Matchers.newClassNameMatcher(className);
        this.instrumentContext.addClassFileTransformer(matcher, transformCallback);
    }
}
