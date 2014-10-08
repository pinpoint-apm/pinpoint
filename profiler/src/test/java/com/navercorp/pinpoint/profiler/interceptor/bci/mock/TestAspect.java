package com.nhn.pinpoint.profiler.interceptor.bci.mock;

import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.profiler.interceptor.aspect.Aspect;
import com.nhn.pinpoint.profiler.interceptor.aspect.JointPoint;
import com.nhn.pinpoint.profiler.interceptor.aspect.PointCut;

import java.util.Collections;
import java.util.Map;

/**
 * @author emeroad
 */
@Aspect
public abstract class TestAspect extends Original {

	@PointCut
	public void testVoid() {
		touchBefore();
		__testVoid();
		touchAfter();
	}

	@JointPoint
	abstract void __testVoid();


	@PointCut
	public int testInt() {
		touchBefore();
		final int result = __testInt();
		touchAfter();
		return result;
	}

	@JointPoint
	abstract int __testInt();


	@PointCut
	public String testString() {
		touchBefore();
		String s = __testString();
		touchAfter();
		return s;
	}

	@JointPoint
	abstract String __testString();

	@PointCut
	public int testUtilMethod() {
		touchBefore();
		int result = __testInt();
		utilMethod();
		touchAfter();
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
