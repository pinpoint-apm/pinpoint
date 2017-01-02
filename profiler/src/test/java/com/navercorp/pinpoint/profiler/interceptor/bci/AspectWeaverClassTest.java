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

package com.navercorp.pinpoint.profiler.interceptor.bci;

import com.navercorp.pinpoint.profiler.instrument.aspect.AspectWeaverClass;

import com.navercorp.pinpoint.profiler.util.LoaderUtils;
import javassist.*;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

public class AspectWeaverClassTest {

    private final String ORIGINAL = "com.navercorp.pinpoint.profiler.interceptor.bci.mock.Original";
    private final String ORIGINAL_SUB = "com.navercorp.pinpoint.profiler.interceptor.bci.mock.OriginalSub";

    private final String ASPECT = "com.navercorp.pinpoint.profiler.interceptor.bci.mock.TestAspect";
    private final String ASPECT_NO_EXTENTS = "com.navercorp.pinpoint.profiler.interceptor.bci.mock.TestAspect_NoExtents";
    private final String ASPECT_EXTENTS_SUB = "com.navercorp.pinpoint.profiler.interceptor.bci.mock.TestAspect_ExtentsSub";

    private final String ERROR_ASPECT1 = "com.navercorp.pinpoint.profiler.interceptor.bci.mock.ErrorAspect";
    private final String ERROR_ASPECT2 = "com.navercorp.pinpoint.profiler.interceptor.bci.mock.ErrorAspect2";

    private final String ERROR_ASPECT_INVALID_EXTENTS= "com.navercorp.pinpoint.profiler.interceptor.bci.mock.ErrorAspect_InvalidExtents";

    public Object createAspect(String originalName, String aspectName)  {
        try {
            ClassPool classPool = new ClassPool(true);
            Loader loader = getLoader(classPool);

            CtClass ctOriginal = classPool.get(originalName);
            CtClass ctAdvice = classPool.get(aspectName);

            AspectWeaverClass weaver = new AspectWeaverClass();

            weaver.weaving(ctOriginal, ctAdvice);

            Class aClass = loader.loadClass(originalName);
            return aClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Loader getLoader(ClassPool pool) {
        return LoaderUtils.createLoader(pool);
    }

    private Object createDefaultAspect() {
        return createAspect(ORIGINAL, ASPECT);
    }

    @Test
    public void testVoid() throws Exception {

        Object aspectObject = createDefaultAspect();

        invoke(aspectObject, "testVoid");
        assertBeforeTouchCount(aspectObject, 1);
        assertAfterTouchCount(aspectObject, 1);

    }




    @Test
    public void testInt() throws Exception {

        Object aspectObject = createDefaultAspect();

        int returnValue = (Integer)invoke(aspectObject, "testInt");
        Assert.assertEquals(1, returnValue);

        assertBeforeTouchCount(aspectObject, 1);
        assertAfterTouchCount(aspectObject, 1);
    }


    @Test
     public void testString() throws Exception {

        Object aspectObject = createDefaultAspect();

        String returnValue = (String) invoke(aspectObject, "testString");
        Assert.assertEquals(returnValue, "testString");

        assertBeforeTouchCount(aspectObject, 1);
        assertAfterTouchCount(aspectObject, 1);
    }

    @Test
    public void testUtilMethod() throws Exception {

        Object aspectObject = createDefaultAspect();

        int returnValue = (Integer)invoke(aspectObject, "testUtilMethod");
        Assert.assertEquals(1, returnValue);

        assertBeforeTouchCount(aspectObject, 1);
        assertAfterTouchCount(aspectObject, 1);
    }

    @Test
    public void testNoTouch() throws Exception {

        Object aspectObject = createDefaultAspect();

        Object returnValue = invoke(aspectObject, "testNoTouch");
        Assert.assertEquals(null, returnValue);

        assertBeforeTouchCount(aspectObject, 0);
        assertAfterTouchCount(aspectObject, 0);
    }

    @Test
    public void testInternalMethod() throws Exception {

        Object aspectObject = createDefaultAspect();

        Object returnValue = invoke(aspectObject, "testInternalMethod");
        Assert.assertEquals(null, returnValue);

        assertBeforeTouchCount(aspectObject, 1);
        assertAfterTouchCount(aspectObject, 1);
    }

    @Test
    public void testMethodCall() throws Exception {

        Object aspectObject = createDefaultAspect();

        invoke(aspectObject, "testMethodCall");

    }

    @Test(expected = Exception.class)
    public void testSignatureMiss() throws Exception {
        createAspect(ORIGINAL, ERROR_ASPECT1);
    }

    @Test(expected = Exception.class)
    public void testInternalTypeMiss() throws Exception {

        createAspect(ORIGINAL, ERROR_ASPECT2);

    }

    @Test
    public void testNo_extents() throws Exception {

        Object aspectObject = createAspect(ORIGINAL, ASPECT_NO_EXTENTS);

        Object returnValue = invoke(aspectObject, "testVoid");
        Assert.assertEquals(null, returnValue);

    }

    @Test
    public void testExtents_Sub() throws Exception {

        Object aspectObject = createAspect(ORIGINAL_SUB, ASPECT_EXTENTS_SUB);

        Object returnValue = invoke(aspectObject, "testVoid");
        Assert.assertEquals(null, returnValue);

    }

    @Test(expected = Exception.class)
    public void testInvalid_extents() throws Exception {

        Object aspectObject = createAspect(ORIGINAL, ERROR_ASPECT_INVALID_EXTENTS);

        Object returnValue = invoke(aspectObject, "testVoid");
        Assert.assertEquals(null, returnValue);

    }




    private Object invoke(Object o, String methodName, Object... args) {
        try {
            Class<?> clazz = o.getClass();
            Method method = clazz.getMethod(methodName);
            return method.invoke(o, args);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void assertBeforeTouchCount(Object aspectObject, int count) {
        int touchCount = (Integer)invoke(aspectObject, "getTouchBefore");
        Assert.assertEquals(touchCount, count);
    }

    private void assertAfterTouchCount(Object aspectObject, int count) {
        int touchCount = (Integer)invoke(aspectObject, "getTouchAfter");
        Assert.assertEquals(touchCount, count);
    }


}