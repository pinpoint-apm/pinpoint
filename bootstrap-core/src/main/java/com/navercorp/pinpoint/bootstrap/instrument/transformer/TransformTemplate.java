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
import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author emeroad
 */
public class TransformTemplate implements TransformOperations {

    private final InstrumentContext instrumentContext;

    public TransformTemplate(InstrumentContext instrumentContext) {
        this.instrumentContext = Assert.requireNonNull(instrumentContext, "instrumentContext");
    }

    protected InstrumentContext getInstrumentContext() {
        return instrumentContext;
    }

    /**
     * @deprecated Since 1.9.0 Use {@link #transform(String, Class)}
     */
    @Deprecated
    @Override
    public void transform(String className, TransformCallback transformCallback) {
        Assert.requireNonNull(className, "className");
        Assert.requireNonNull(transformCallback, "transformCallback");

        final Matcher matcher = Matchers.newClassNameMatcher(className);
        this.instrumentContext.addClassFileTransformer(matcher, transformCallback);
    }

    @Override
    public void transform(String className, Class<? extends TransformCallback> transformCallbackClass) {
        Assert.requireNonNull(className, "className");
        Assert.requireNonNull(transformCallbackClass, "transformCallbackClass");

        final Matcher matcher = Matchers.newClassNameMatcher(className);

        TransformCallbackChecker.validate(transformCallbackClass);

        // release class reference
        final String transformCallbackName = transformCallbackClass.getName();
        this.instrumentContext.addClassFileTransformer(matcher, transformCallbackName);
    }

//    @Override
//    public void transform(String className, Class<? extends TransformCallback> transformCallbackClass, Object[] parameters) {
//        Assert.requireNonNull(className, "className");
//        Assert.requireNonNull(transformCallbackClass, "transformCallbackClass");
//
////        if (ParameterUtils.hasNull(parameters)) {
////            throw new IllegalArgumentException("null parameter not supported");
////        }
//
//        final Class<?>[] parameterType = ParameterUtils.toClass(parameters);
//        // commons-lang
//        // ConstructorUtils.getMatchingAccessibleConstructor()
//
//        transform(className, transformCallbackClass, parameters, parameterType);
//    }

    @Override
    public void transform(String className, Class<? extends TransformCallback> transformCallbackClass, Object[] parameters, Class<?>[] parameterTypes) {
        Assert.requireNonNull(className, "className");
        Assert.requireNonNull(transformCallbackClass, "transformCallbackClass");



        TransformCallbackChecker.validate(transformCallbackClass, parameterTypes);
        if (ParameterUtils.hasNull(parameterTypes)) {
            throw new IllegalArgumentException("null parameterType not supported");
        }
        ParameterUtils.checkParameterType(parameterTypes);


        final Matcher matcher = Matchers.newClassNameMatcher(className);

        // release class reference
        final String transformCallbackName = transformCallbackClass.getName();
        this.instrumentContext.addClassFileTransformer(matcher, transformCallbackName, parameters, parameterTypes);
    }
}
