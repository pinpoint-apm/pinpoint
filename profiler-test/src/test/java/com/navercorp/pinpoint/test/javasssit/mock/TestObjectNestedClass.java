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
package com.navercorp.pinpoint.test.javasssit.mock;

import java.util.concurrent.Callable;

/**
 * @author jaehong.kim
 *
 */
public class TestObjectNestedClass {

    public void annonymousInnerClass() {
        new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                return null;
            }
        };

        new Runnable() {
            public void run() {
            }
        };
    }
    
    public void annonymousInnerClass2() {
        new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                return null;
            }
        };

        new Runnable() {
            public void run() {
            }
        };
    }

    
    public void instanceInnerClass() {
        new InstanceInner();
    }
    
    class InstanceInner {}


    public void localInnerClass() {
        class LocalInner {}

        new LocalInner();
    }
    
    public void localInnerClass2() {
        class LocalInner {}

        new LocalInner();
    }
    
    public void staticNestedClass() {
        new StaticNested();
    }
    
    static class StaticNested{}


    public void enclosingMethod(String s, int i) {
        class LocalInner {}

        new LocalInner();
    }
}
