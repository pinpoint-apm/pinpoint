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
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.List;

import com.navercorp.pinpoint.common.AnnotationKey;



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
    public void verifyTraceBlock(BlockType type, String serviceType, Member api, String rpc, String endPoint, String remoteAddr, String destinationId, ExpectedAnnotation... annotations);
    public void verifyTraceBlock(BlockType type, String serviceType, String methodSignature, String rpc, String endPoint, String remoteAddr, String destinationId, ExpectedAnnotation... annotations);
    public void verifyApi(String serviceType, Member api, Object...args);
    public void ignoreServiceType(String serviceType);
    public void printBlocks(PrintStream out);
    public void printCache(PrintStream out);
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
        
        public static ExpectedAnnotation[] cachedArgs(Object... args) {
            ExpectedAnnotation[] annotations = new ExpectedAnnotation[args.length];
            
            for (int i = 0; i < args.length; i++) {
                annotations[i] = ExpectedAnnotation.annotation(AnnotationKey.getCachedArgs(i).getName(), args[i]);
            }
            
            return annotations;
        }

        
        public static ExpectedAnnotation sql(String query, String output, Object... bindValues) {
            return new ExpectedSql(query, output, bindValues);
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
    
    public static class ExpectedSql extends ExpectedAnnotation {
        private final String output;
        private final Object[] bindValues;
        
        public ExpectedSql(String query, String output, Object[] bindValues) {
            super(AnnotationKey.SQL_ID.getName(), query);
            this.output = output;
            this.bindValues = bindValues;
        }
        
        public String getQuery() {
            return (String)getValue();
        }

        public String getOutput() {
            return output;
        }

        public Object[] getBindValues() {
            return bindValues;
        }
        
        public String getBindValuesAsString() {
            if (bindValues.length == 0) {
                return null;
            }
            
            StringBuilder builder = new StringBuilder();
            
            boolean first = true;
            
            for (Object o : bindValues) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                
                builder.append(o);
            }
            
            return builder.toString();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(getKeyName());
            builder.append("=[query=");
            builder.append(getQuery());
            builder.append(", output=");
            builder.append(output);
            builder.append(", bindValues");
            builder.append(Arrays.toString(bindValues));
            builder.append("]");
            
            return builder.toString();
        }
    }
}
