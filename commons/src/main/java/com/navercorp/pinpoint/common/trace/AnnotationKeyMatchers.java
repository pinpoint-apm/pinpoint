/*
 * Copyright 2014 NAVER Corp.
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


import com.navercorp.pinpoint.common.util.AnnotationKeyUtils;

/**
 * @author Jongho Moon
 *
 */
public final class AnnotationKeyMatchers {
    
    private static class ExactMatcher implements AnnotationKeyMatcher {
        private final int code;
        
        public ExactMatcher(int code) {
            this.code = code;
        }
    
        @Override
        public boolean matches(int code) {
            return this.code == code;
        }
    
        @Override
        public String toString() {
            return "ExactMatcher(" + code + ")";
        }
    }

    public static final AnnotationKeyMatcher NOTHING_MATCHER = new AnnotationKeyMatcher() {
        @Override
        public boolean matches(int code) {
            return false;
        }
    
        @Override
        public String toString() {
            return "NOTHING_MATCHER";
        }
    };
    public static final AnnotationKeyMatcher ARGS_MATCHER = new AnnotationKeyMatcher() {
        @Override
        public boolean matches(int code) {
            return AnnotationKeyUtils.isArgsKey(code);
        }
    
        @Override
        public String toString() {
            return "ARGS_MATCHER";
        }
    };

    private AnnotationKeyMatchers() { }

    public static AnnotationKeyMatcher exact(AnnotationKey key) {
        return new AnnotationKeyMatchers.ExactMatcher(key.getCode());
    }

    public static AnnotationKeyMatcher exact(int code) {
        return new AnnotationKeyMatchers.ExactMatcher(code);
    }

}
