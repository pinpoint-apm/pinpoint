package com.nhn.pinpoint.profiler.interceptor.bci;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;

/**
 * @author emeroad
 */
public class JavaAssistTest {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());


	@Test
	public void afterCatch() throws NotFoundException, CannotCompileException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException, ClassNotFoundException {
		ClassPool pool = new ClassPool(true);
		Loader loader = getLoader(pool);

		CtClass ctClass = pool.get("com.nhn.pinpoint.profiler.interceptor.bci.TestObject");
		CtClass object = pool.get("java.lang.String");

		logger.debug("target:{}", ctClass);

//		JavaAssistUtils.getCtParameter(new String[]]"java.lang", aDefault);

		CtMethod callA = ctClass.getDeclaredMethod("callA", null);
		logger.debug("callA:{}", callA);
//		callA.addLocalVariable("__test", object);
		final String before = "{ java.lang.Throwable __throwable = null; java.lang.String __test = \"abc\"; System.out.println(\"before\" + __test);";
//		callA.insertBefore();
//		callA.insertAfter("System.out.println(\"after\" + __test);");
//		final String after =  "finally {System.out.println(\"after\" + __test);}}";
		final String after = "}";
//		callA.addCatch();

//		callA.addCatch("System.out.println(\"after\");", pool.get("java.lang.Throwable"));
		callA.instrument(new ExprEditor() {
			@Override
			public void edit(MethodCall m) throws CannotCompileException {
				System.out.println(m.getClassName());
				try {
					System.out.println(m.getMethod().toString());
				} catch (NotFoundException e) {
					e.printStackTrace();
				}
				System.out.println(m.getMethodName());
				m.replace(before + " try {$_ = $proceed($$); System.out.println(\"end---\"+ $_);} catch (java.lang.Throwable ex) { __throwable = ex; System.out.println(\"catch\"); } " + after);
			}
		});


		Class aClass = loader.loadClass(ctClass.getName());
		java.lang.reflect.Method callA1 = aClass.getMethod("callA");
		Object target = aClass.newInstance();
		Object result = callA1.invoke(target);
		logger.debug("result:{}", result);
	}

	@Test
	 public void afterCatch2() throws NotFoundException, CannotCompileException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException, ClassNotFoundException {
		ClassPool pool = new ClassPool(true);
		Loader loader = getLoader(pool);
		CtClass ctClass = pool.get("com.nhn.pinpoint.profiler.interceptor.bci.TestObject");
		CtClass object = pool.get("java.lang.String");
		logger.debug("target:{}", ctClass);

//		JavaAssistUtils.getCtParameter(new String[]]"java.lang", aDefault);

		CtMethod callA = ctClass.getDeclaredMethod("callA", null);
		logger.debug("callA:{}", callA);
		callA.addLocalVariable("__test", object);
		callA.insertBefore("{ __test = \"abc\"; System.out.println(\"before\" + __test); }");
		callA.insertAfter("{ System.out.println(\"after\"); }");
		callA.addCatch("{ System.out.println(\"catch\"); throw $e; }", pool.get("java.lang.Throwable"));

		Class aClass = loader.loadClass(ctClass.getName());
		java.lang.reflect.Method callA1 = aClass.getMethod("callA");
		Object target = aClass.newInstance();
		Object result = callA1.invoke(target);
		logger.debug("result:{}", result);

	}

	@Test
	public void around() throws NotFoundException, CannotCompileException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException, ClassNotFoundException {
		ClassPool pool = new ClassPool(true);
		Loader loader = getLoader(pool);


		CtClass ctClass = pool.get("com.nhn.pinpoint.profiler.interceptor.bci.TestObject");
		CtClass object = pool.get("java.lang.String");
		logger.debug("target:{}", ctClass);

//		JavaAssistUtils.getCtParameter(new String[]]"java.lang", aDefault);

		CtMethod callA = ctClass.getDeclaredMethod("callA", null);
		logger.debug("callA:{}", callA);
		callA.addLocalVariable("__test", object);
		String inti = "__test = \"abc\";";
//		callA.insertBefore("__test = \"abc\";);
		callA.insertBefore("{com.nhn.pinpoint.profiler.interceptor.bci.TestObject.before();}");
		callA.insertAfter("{com.nhn.pinpoint.profiler.interceptor.bci.TestObject.after();}");
		callA.addCatch("{ com.nhn.pinpoint.profiler.interceptor.bci.TestObject.callCatch(); throw $e; }", pool.get("java.lang.Throwable"));

		Class aClass = loader.loadClass(ctClass.getName());
		java.lang.reflect.Method callA1 = aClass.getMethod("callA");
		Object target = aClass.newInstance();
		try {
			Object result = callA1.invoke(target);
			logger.debug("result:{}", result);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

//		ctClass.debug("./TestObjectAAA.class");

	}

	private Loader getLoader(ClassPool pool) {
		Loader loader = new Loader(pool);
		loader.delegateLoadingOf("org.apache.log4j.");
		return loader;
	}
}
