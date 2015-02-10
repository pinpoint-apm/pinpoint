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
package com.navercorp.pinpoint.bootstrap.plugin.test;

import java.io.PrintStream;
import java.lang.reflect.Method;

import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.ServiceType;



/**
 * @author Jongho Moon
 *
 */
public interface PluginTestVerifier {
    public void verifyServerType(ServiceType serviceType);
    public void verifySpanCount(int count);
    public void verifySpan(ServiceType serviceType, ExpectedAnnotation...annotations);
    public void verifySpanEvent(ServiceType serviceType, ExpectedAnnotation...annotations);
    public void verifyApi(ServiceType serviceType, Method method, Object... args);
    public void printSpans(PrintStream out);
    public void printApis(PrintStream out);
    public void initialize(boolean initializeTraceObject);
    public void cleanUp(boolean detachTraceObject);
    
    public static class ExpectedAnnotation {
        public static ExpectedAnnotation annotation(AnnotationKey key, Object value) {
            return new ExpectedAnnotation(key.getCode(), value);
        }

        private final int key;
        private final Object value;
        
        public ExpectedAnnotation(int key, Object value) {
            this.key = key;
            this.value = value;
        }

        public int getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }
}
