/*
 * Copyright 2019 NAVER Corp.
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

/**
 * @author HyunGil Jeong
 */
public class AnnotationKeyProvider {

    private static final AnnotationKeyLocator UNREGISTERED = new AnnotationKeyLocator() {
        @Override
        public AnnotationKey findAnnotationKey(int code) {
            throw new IllegalStateException("AnnotationKeyRegistry not registered");
        }

        @Override
        public AnnotationKey findAnnotationKeyByName(String keyName) {
            throw new IllegalStateException("AnnotationKeyRegistry not registered");
        }

        @Override
        public AnnotationKey findApiErrorCode(int annotationCode) {
            throw new IllegalStateException("AnnotationKeyRegistry not registered");
        }
    };

    private static AnnotationKeyLocator registry = UNREGISTERED;

    private AnnotationKeyProvider() {
        throw new AssertionError();
    }

    public static AnnotationKey getByCode(int annotationKeyCode) {
        AnnotationKey annotationKey = registry.findAnnotationKey(annotationKeyCode);
        if (AnnotationKey.UNKNOWN == annotationKey) {
            throw new IllegalStateException("Unknown AnnotationKey code: " + annotationKeyCode);
        }
        return annotationKey;
    }

    static void register(AnnotationKeyLocator annotationKeyRegistry) {
        registry = annotationKeyRegistry;
    }
}
