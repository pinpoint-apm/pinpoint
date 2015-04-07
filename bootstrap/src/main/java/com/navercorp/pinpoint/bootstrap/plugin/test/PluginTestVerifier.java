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
import java.util.List;

import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.ServiceType;



/**
 * @author Jongho Moon
 *
 */
public interface PluginTestVerifier {
    public void verifyServerType(String expected);
    public void verifyServerInfo(String expected);
    public void verifyConnector(String protocol, int port);
    public void verifyService(String context, List<String> libs);
    public void verifyTraceBlockCount(int expected);
    public void verifyTraceBlock(BlockType type, String serviceType, ExpectedAnnotation...annotations);
    public void verifyTraceBlock(BlockType type, String serviceType, Method method, String rpc, String endPoint, String remoteAddr, String destinationId, ExpectedAnnotation... annotations);
    public void verifyTraceBlock(BlockType type, String serviceType, String methodSignature, String rpc, String endPoint, String remoteAddr, String destinationId, ExpectedAnnotation... annotations);
    public void verifyApi(String serviceType, Method method, Object...args);
    public void popSpan();
    public void printBlocks(PrintStream out);
    public void printCachedApis(PrintStream out);
    public void initialize(boolean initializeTraceObject);
    public void cleanUp(boolean detachTraceObject);
    
    public enum BlockType {
        ROOT,
        EVENT
    }
    
    public static class ExpectedAnnotation {
        public static ExpectedAnnotation annotation(String annotationKeyName, Object value) {
            return new ExpectedAnnotation(annotationKeyName, value);
        }

        public static ExpectedAnnotation[] args(Object... args) {
            ExpectedAnnotation[] annotations = new ExpectedAnnotation[args.length];
            
            for (int i = 0; i < args.length; i++) {
                annotations[i] = ExpectedAnnotation.annotation(AnnotationKey.getArgs(i).getName(), args[i]);
            }
            
            return annotations;
        }

        private final String keyName;
        private final Object value;
        
        public ExpectedAnnotation(String keyName, Object value) {
            this.keyName = keyName;
            this.value = value;
        }

        public String getKeyName() {
            return keyName;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return keyName + "=" + value;
        }
        
    }
}
