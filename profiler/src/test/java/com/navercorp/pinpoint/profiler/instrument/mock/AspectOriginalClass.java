/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.instrument.mock;

import java.util.Collections;
import java.util.Map;

/**
 * @author jaehong.kim
 */
public class AspectOriginalClass {

    public int touchVoid = 0;
    public int touchInt = 0;
    public int touchUtil = 0;

    public int touchBefore;
    public int touchAfter;

    public void testVoid() {
        System.out.println("Original testVoid()");
        touchVoid++;
    }

    public int getTouchVoid() {
        System.out.println("Original getTouchVoid()");
        return touchVoid;
    }

    public int testInt() {
        System.out.println("Original testInt()");
        return ++touchInt;
    }

    public int getTouchInt() {
        System.out.println("Original getTouchInt()");
        return touchInt;
    }

    public String testString() {
        System.out.println("Original testString()");
        return "testString";
    }

    public int testUtilMethod() {
        System.out.println("Original testUtilMethod()");
        return ++touchUtil;
    }

    void touchBefore() {
        System.out.println("Original touchBefore()");
        touchBefore++;
    }

    public int getTouchBefore() {
        System.out.println("Original getTouchBefore()");
        return touchBefore;
    }

    void touchAfter() {
        System.out.println("Original touchAfter()");
        touchAfter++;
    }

    public int getTouchAfter() {
        System.out.println("getTouchAfter");
        return touchAfter;
    }

    public void testNoTouch() {
        System.out.println("Original testNoTouch()");
    }

    public void methodA() {
        System.out.println("---a");
    }

    public void methodB() {
        System.out.println("---b");
    }


    public void testInternalMethod() {
        System.out.println("Original testInternalMethod()");
        touchBefore();
        touchAfter();
        //super
        String s = toString();
    }

    public int testSignatureMiss() {
        System.out.println("Original testSignatureMiss()");
        return -1;
    }

    public void testMethodCall() {
        System.out.println("Original testMethodCall()");
    }

    public Map<String, String> testGeneric() {
        System.out.println("Original testGeneric()");
        return Collections.emptyMap();
    }
}
