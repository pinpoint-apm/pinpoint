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

package com.navercorp.pinpoint.test.javasssit;

import java.util.List;

/**
 * @author emeroad
 */
public class TestClass<T> {
    private int a;
    private List<T> b;
    private String[] stringArray;


    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int inlineClass(final int a) {
        Comparable c = new Comparable() {
            @Override
            public int compareTo(Object o) {
                return a;
            }
        };
        return c.compareTo(null);
    }

    private class InnerClass {
        private String str;

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }
    }

    public void setB(List<T> b) {
        this.b = b;
    }

    public void setStringArray(String[] stringArray) {
        this.stringArray = stringArray;
    }
}
