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

import com.navercorp.pinpoint.bootstrap.instrument.aspect.Aspect;
import com.navercorp.pinpoint.bootstrap.instrument.aspect.JointPoint;
import com.navercorp.pinpoint.bootstrap.instrument.aspect.PointCut;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.Map;

/**
 * @author jaehong.kim
 */
@Aspect
public abstract class AspectInterceptorExtendSubClass extends AspectOriginalSubClass {

    @PointCut
    public void testVoid() {
        __testVoid();
    }

    @JointPoint
    abstract void __testVoid();

    @PointCut
    public int testInt() {
        final int result = __testInt();
        return result;
    }

    @JointPoint
    abstract int __testInt();


    @PointCut
    public String testString() {
        String s = __testString();
        return s;
    }

    @JointPoint
    abstract String __testString();

    @PointCut
    public int testUtilMethod() {
        int result = __testInt();
        utilMethod();
        return result;
    }

    private String utilMethod() {
        return "Util";
    }

    @PointCut
    public void testNoTouch() {
         __testVoid();
    }

    @PointCut
    public void testInternalMethod() {
         __testVoid();
    }

    @PointCut
    public void testMethodCall() {
         BytesUtils.toBytes("test");
        __testMethodCall();
    }

    @JointPoint
    abstract void __testMethodCall();

    @PointCut
    public Map<String, String> testGeneric() {
         return null;
    }

}