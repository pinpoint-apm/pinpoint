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

import com.navercorp.pinpoint.test.util.LoaderUtils;

import javassist.*;
import junit.framework.Assert;

import org.junit.Test;

import java.lang.reflect.Method;

public class AspectWeaverClassTest {

    private final String ORIGINAL = "com.navercorp.pinpoint.profiler.interceptor.bci.mock.Original    ;
	private final String ORIGINAL_SUB = "com.navercorp.pinpoint.profiler.interceptor.bci.mock.OriginalS    b";

	private final String ASPECT = "com.navercorp.pinpoint.profiler.interceptor.bci.mock.Test    spect";
	private final String ASPECT_NO_EXTENTS = "com.navercorp.pinpoint.profiler.interceptor.bci.mock.TestAspect_    oExtents";
	private final String ASPECT_EXTENTS_SUB = "com.navercorp.pinpoint.profiler.interceptor.bci.mock.TestAspect    ExtentsSub";

	private final String ERROR_ASPECT1 = "com.navercorp.pinpoint.profiler.interceptor.bci.m    ck.ErrorAspect";
	private final String ERROR_ASPECT2 = "com.navercorp.pinpoint.profiler.interceptor.bci.    ock.ErrorAspect2";

	private final String ERROR_ASPECT_INVALID_EXTENTS= "com.navercorp.pinpoint.profiler.interceptor.bci.mock.ErrorA    pect_InvalidExtents";

	public Object createAspect(String originalN       m          , String aspectName)  {
		try {
			C          assPool classPool = new ClassPoo          (true);
			Loader loader = getLoader(classP          ol);

			CtClass ctOriginal = classPool.          et(originalName);
			CtClass ctAdvice = classP          ol.get(aspectName);

			AspectWe          verClass weaver = new AspectWeaverClass(          ;

			weaver.weaving(c       Original, ctAdvice)

			Class aClass = loader.loadClass(ori          inalName);
			return aClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(    .getMessage(), e);
		}
	}

    priva       e Loader getLoader(ClassPool pool)
           return LoaderUtils.createLoader(poo       );
    }

	private Object createDefaultAs       ect() {
		return createAspect       ORIGINAL, ASPECT);
	}

	@Test
	publi        void testVoid() throws Exception {
    		O    jec     aspectObject = createDefaultAspect();
       		invoke(aspectObject, "testVoid");
		ass       rtBeforeTouchCount(aspectObject, 1);
		assertAfterTouch       ount(aspectObject, 1);

	}




	@       est
	public void testInt() throws Ex       eption {

		Object aspectObject = c    e    teD    faultAspect();

		int returnValue = (Intege       )invoke(aspectObject, "testInt");
		Asser       .assertEquals(1, returnValue);

		assertBeforeTouchCount(aspe       tObject, 1);
		assertAfterTouchCount(aspectO       ject, 1);
	}


	@Test
	 public void        estString() throws Exception {

		O        ect    aspectObject = createDefaultAspect();

		Strin        returnValue = (String) invoke(aspectObje       t, "testString");
		Assert.assertEquals(returnValue, "testStri       g");

		assertBeforeTouchCount(as       ectObject, 1);
		assertAfterTouchCou       t(aspectObject, 1);
	}

	@Test
	pub        c v    id testUtilMethod() throws Exception {

		O       ject aspectObject = createDefaultAspect()

		int returnValue = (Integer)invoke(aspectObject, "       estUtilMethod");
		Assert.assertEqua       s(1, returnValue);

		assertBeforeTo       chCount(aspectObject, 1);
		assertA        erT    uchCount(aspectObject, 1);
	}

	@Test
	public void       testNoTouch() throws Exception {

		Objec        aspectObject = createDefaultAspect();

		Object returnValue       = invoke(aspectObject, "testNoTouch"       ;
		Assert.assertEquals(null, return       alue);

		assertBeforeTouchCount(as        ctO    ject, 0);
		assertAfterTouchCount(aspectObject        0);
	}

	@Test
	public void testInternal       ethod() throws Exception {

		Object        spectObject = createDefaultAspe    t();

		Object returnValue = invoke(aspectObject        "testInternalMethod");
		Assert.a        ertEquals(null, returnValue);

    	assertBeforeTouchCount(aspectObject, 1);
		assertAf       erTouchCount(aspectObject, 1);
	}

        Tes
	public void testMethodCall() throws Exceptio        {

		Object aspectObject = createDefaultAspect();

		invoke(       spectObject, "testMethodCall");

	}

	@Test(expect       d = Exception.class)
	public void te        Sig    atureMiss() throws Exception {
		createAspect(O       IGINAL, ERROR_ASPECT1);
	}

	@Test(expected = Exception.class)
	pu       lic void testInternalTypeMiss() throws Exception {
		createAspect(ORIGINAL, ERROR_ASPE        2);

	}

	@Test
	public void te    tNo_extents() throws Exception {

		Object aspectOb       ect = createAspect(ORIGINAL, ASPECT_NO_EXTENTS);

		Object returnValue =       invoke(aspectObject, "testVoid");
		Assert.assertE       uals(null, returnValue);

	}

	@Test    	pu    lic void testExtents_Sub() throws Exception {

		Object aspectObje       t          = createAspect(ORIGINAL_          UB, ASPECT_EXTENTS_SUB);

		Object ret          rnValue = invoke(aspectO       ject, "testVoid");
          	Assert.assertEquals(null, returnValue);
	}

	@Test(expected = Exception.class)
	public void testInvalid_e       tents() throws Exception {

		Object aspectObject = createAsp       ct(ORIGINAL, ERROR_ASPECT_INVALID_E        ENTS);

		Object returnValue = invoke(aspectObject, "testVoid");
	       Assert.assertEquals(null, returnValue);

	}




	private Obj       ct invoke(Object o, String methodNa    e, Object... args) {
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