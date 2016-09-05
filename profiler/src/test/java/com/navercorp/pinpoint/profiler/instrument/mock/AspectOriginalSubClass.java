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
public class AspectOriginalSubClass extends AspectOriginalClass {

    @Override
    public void testVoid() {
        System.out.println("Original testVoid()");
        touchVoid++;
    }

    @Override
    public int getTouchVoid() {
        System.out.println("Original getTouchVoid()");
        return touchVoid;
    }

    @Override
    public int testInt() {
        System.out.println("Original testInt()");
        return ++touchInt;
    }

    @Override
    public int getTouchInt() {
        System.out.println("Original getTouchInt()");
        return touchInt;
    }

    @Override
    public String testString() {
        System.out.println("Original testString()");
        return "testString";
    }

    @Override
    public int testUtilMethod() {
        System.out.println("Original testUtilMethod()");
        return ++touchUtil;
    }

    @Override
    void touchBefore() {
        System.out.println("Original touchBefore()");
        touchBefore++;
    }

    @Override
    public int getTouchBefore() {
        System.out.println("Original getTouchBefore()");
        return touchBefore;
    }

    @Override
    void touchAfter() {
        System.out.println("Original touchAfter()");
        touchAfter++;
    }

    @Override
    public int getTouchAfter() {
        System.out.println("Original getTouchAfter()");
        return touchAfter;
    }

    @Override
    public void testNoTouch() {
        System.out.println("Original testNoTouch()");
    }

    @Override
    public void methodA() {
        System.out.println("---a");
    }

    @Override
    public void methodB() {
        System.out.println("---b");
    }

    @Override
    public void testInternalMethod() {
        System.out.println("Original testInternalMethod()");
        touchBefore();
        touchAfter();
        //super
        String s = toString();
    }

    @Override
    public int testSignatureMiss() {
        System.out.println("Original testSignatureMiss()");
        return -1;
    }

    @Override
    public void testMethodCall() {
        System.out.println("Original testMethodCall()");
    }

    @Override
    public Map<String, String> testGeneric() {
        System.out.println("Original testGeneric()");
        return Collections.emptyMap();
    }
}
