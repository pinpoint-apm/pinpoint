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

package com.navercorp.pinpoint.profiler.modifier.arcus;

import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;

/**
 * @author emeroad
 */
public final class ParameterUtils {

    private ParameterUtils() {
    }

    public static int findFirstString(MethodInfo method, int maxIndex) {
        return findFirstClass("java.lang.String", method, maxIndex);
    }

    public static int findFirstClass(String className, MethodInfo method, int maxIndex) {
        if (className == null) {
            throw new NullPointerException("className must not be null");
        }
        if (method == null) {
            return -1;
        }
        final String[] methodParams = method.getParameterTypes();
        final int minIndex = Math.min(methodParams.length, maxIndex);
        for (int i =0; i < minIndex; i++) {
            if (className.equals(methodParams[i])) {
                return i;
            }
        }
        return -1;
    }
}
