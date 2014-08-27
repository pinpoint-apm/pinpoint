package com.nhn.pinpoint.profiler.junit4;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.nhn.pinpoint.profiler.util.TestClassLoader;

/**
 *
 * @author Hyun Jeong
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PinpointTestClassLoader {

	Class<? extends TestClassLoader> loader() default TestClassLoader.class;
	
	Class<? extends TestClassLoader> value() default TestClassLoader.class;
	
}
