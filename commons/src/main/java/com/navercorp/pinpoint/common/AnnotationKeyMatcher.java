/**
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
package com.navercorp.pinpoint.common;

/**
 * @author Jongho Moon
 *
 */
public interface AnnotationKeyMatcher {
    public boolean matches(int code);
    
    public static class ExactMatcher implements AnnotationKeyMatcher {
        private final int code;
        
        public ExactMatcher(AnnotationKey key) {
            this.code = key.getCode();
        }

        @Override
        public boolean matches(int code) {
            return this.code == code;
        }
    }
    
    public static final AnnotationKeyMatcher NOTHING_MATCHER = new AnnotationKeyMatcher() {
        @Override
        public boolean matches(int code) {
            return false;
        }
    };
    
    public static final AnnotationKeyMatcher ARGS_MATCHER = new AnnotationKeyMatcher() {
        @Override
        public boolean matches(int code) {
            return AnnotationKey.isArgsKey(code);
        }
    };
}
