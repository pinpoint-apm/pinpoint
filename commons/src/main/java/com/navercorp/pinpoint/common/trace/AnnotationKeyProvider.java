/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.common.trace;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author HyunGil Jeong
 */
public class AnnotationKeyProvider {

    private static final AnnotationKeyProvider INSTANCE = new AnnotationKeyProvider();

    private final ConcurrentMap<Integer, AnnotationKey> codeMap = new ConcurrentHashMap<Integer, AnnotationKey>();

    private AnnotationKeyProvider() {
    }

    public static AnnotationKey getByCode(int annotationKeyCode) {
        AnnotationKey annotationKey = INSTANCE.codeMap.get(annotationKeyCode);
        if (annotationKey == null) {
            throw new IllegalStateException("Unknown AnnotationKey code: " + annotationKeyCode);
        }
        return annotationKey;
    }

    static void register(AnnotationKey annotationKey) {
        if (annotationKey == null) {
            throw new IllegalArgumentException("annotationKey must not be null");
        }
        INSTANCE.addAnnotationKey(annotationKey);
    }

    private void addAnnotationKey(AnnotationKey annotationKey) {
        int code = annotationKey.getCode();
        AnnotationKey prev = codeMap.putIfAbsent(code, annotationKey);
        if (prev != null) {
            throw new IllegalStateException("Duplicate AnnotationKey code: " + code + " found for names: " + prev.getName() + ", " + annotationKey.getName());
        }
    }
}
