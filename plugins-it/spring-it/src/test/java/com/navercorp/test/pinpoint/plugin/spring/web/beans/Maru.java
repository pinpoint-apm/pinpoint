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

package com.navercorp.test.pinpoint.plugin.spring.web.beans;


public class Maru implements Comparable<Maru> {
    private static final String staticField;
    private final String field;
    
    static {
        staticField = System.getProperty("no-such-property", "default value");
    }
    
    public Maru(String field) {
        this.field = field;
    }
    
    public Maru() {
        this.field = null;
    }
    

    private void privateMethod() {
        
    }
    
    protected void protectedMethod() {
        
    }

    public void publicMethod() {
        
    }

    private static void staticPrivateMethod() {
        
    }
    
    public static void staticPublicMethod() {
        
    }

    // To test bridge method
    @Override
    public int compareTo(Maru o) {
        return 0;
    }

    // To test synthetic method
    public class ToTestSyntheticMethod {
        public void callOuterPrivate() {
            privateMethod();
        }
    }
}
