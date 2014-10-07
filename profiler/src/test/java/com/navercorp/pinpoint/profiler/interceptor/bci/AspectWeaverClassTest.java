package com.nhn.pinpoint.profiler.interceptor.bci;

import javassist.*;
import junit.framework.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

public class AspectWeaverClassTest {

	public Object createAspect(String originalName, String aspectName)  {
		try {
			ClassPool classPool = new ClassPool(true);
			Loader loader = new Loader(classPool);

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

	private Object createDefaultAspect() {
		String originalName = "com.nhn.pinpoint.profiler.interceptor.bci.mock.Original";
		String aspectName = "com.nhn.pinpoint.profiler.interceptor.bci.mock.TestAspect";
		return createAspect(originalName, aspectName);
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

	@Test(expected = Exception.class)
	public void testSignatureMiss() throws Exception {

		String originalName = "com.nhn.pinpoint.profiler.interceptor.bci.mock.Original";
		String aspectName = "com.nhn.pinpoint.profiler.interceptor.bci.mock.ErrorAspect";
		createAspect(originalName, aspectName);
	}

	@Test(expected = Exception.class)
	public void testInternalTypeMiss() throws Exception {

		String originalName = "com.nhn.pinpoint.profiler.interceptor.bci.mock.Original";
		String aspectName = "com.nhn.pinpoint.profiler.interceptor.bci.mock.ErrorAspect2";
		createAspect(originalName, aspectName);

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