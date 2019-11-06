/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.instrument.transformer;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.exception.PinpointException;

import java.lang.reflect.Modifier;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class TransformCallbackChecker {
    private TransformCallbackChecker() {
    }

    public static void validate(Class<? extends TransformCallback> transformCallbackClass) {
        validate(transformCallbackClass, null);
    }
    public static void validate(Class<? extends TransformCallback> transformCallbackClass, Class<?>[] parameterTypes) {
        Assert.requireNonNull(transformCallbackClass, "transformCallbackClass");

        // check inner class
        final Class<?> enclosingClass = transformCallbackClass.getEnclosingClass();
        if (enclosingClass != null) {
            // inner class state

            // check static class
            int modifiers = transformCallbackClass.getModifiers();
            if (!Modifier.isStatic(modifiers)) {
                throw new PinpointException("transformCallbackClass must be static inner class. class:" + transformCallbackClass.getName());
            }
        }
//        final Method enclosingMethod = transformCallbackClass.getEnclosingMethod();
//        if (enclosingMethod != null) {
//            throw new PinpointException("Local Inner class not support " + transformCallbackClass.getName());
//        }

        try {
            // check default constructor
            transformCallbackClass.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new PinpointException("constructor not found " + transformCallbackClass.getName(), e);
        }
    }
}
