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
        touchVoid++;
    }

    @Override
    public int getTouchVoid() {
        return touchVoid;
    }

    @Override
    public int testInt() {
        return ++touchInt;
    }

    @Override
    public int getTouchInt() {
        return touchInt;
    }

    @Override
    public String testString() {
        return "testString";
    }

    @Override
    public int testUtilMethod() {
        return ++touchUtil;
    }

    @Override
    void touchBefore() {
        touchBefore++;
    }

    @Override
    public int getTouchBefore() {
        return touchBefore;
    }

    @Override
    void touchAfter() {
        touchAfter++;
    }

    @Override
    public int getTouchAfter() {
        return touchAfter;
    }

    @Override
    public void testNoTouch() {

    }

    @Override
    public void methodA() {
    }

    @Override
    public void methodB() {

    }

    @Override
    public void testInternalMethod() {
        touchBefore();
        touchAfter();
        //super
        String s = toString();
    }

    @Override
    public int testSignatureMiss() {
        return -1;
    }

    @Override
    public void testMethodCall() {

    }

    @Override
    public Map<String, String> testGeneric() {
        return Collections.emptyMap();
    }
}
