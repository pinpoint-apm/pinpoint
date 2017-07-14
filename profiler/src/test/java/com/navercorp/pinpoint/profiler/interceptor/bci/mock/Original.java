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

package com.navercorp.pinpoint.profiler.interceptor.bci.mock;

import java.util.Collections;
import java.util.Map;

/**
 * @author emeroad
 */
public class Original {

    public int touchVoid = 0;
    public int touchInt = 0;
    public int touchUtil = 0;

    public int touchBefore;
    public int touchAfter;

    public void testVoid() {
        touchVoid++;
    }

    public int getTouchVoid() {
        return touchVoid;
    }

    public int testInt() {
        return ++touchInt;
    }

    public int getTouchInt() {
        return touchInt;
    }


    public String testString() {
        return "testString";
    }


    public int testUtilMethod() {
        return ++touchUtil;
    }



    void touchBefore() {
        touchBefore++;
    }

    public int getTouchBefore() {
        return touchBefore;
    }

    void touchAfter() {
        touchAfter++;
    }

    public int getTouchAfter() {
        return touchAfter;
    }

    public void testNoTouch() {

    }


    public void methodA() {
    }

    public void methodB() {

    }


    public void testInternalMethod() {
        touchBefore();
        touchAfter();
        //super
        String s = toString();
    }

    public int testSignatureMiss() {
        return -1;
    }

    public void testMethodCall() {
    }

    public Map<String, String> testGeneric() {
        return Collections.emptyMap();
    }

}
