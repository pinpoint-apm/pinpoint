package com.nhn.pinpoint.profiler.interceptor.bci.mock;

import com.nhn.pinpoint.profiler.interceptor.aspect.Aspect;
import com.nhn.pinpoint.profiler.interceptor.aspect.JointPoint;
import com.nhn.pinpoint.profiler.interceptor.aspect.PointCut;

/**
 * @author emeroad
 */
@Aspect
public abstract class ErrorAspect {

	@PointCut
	public void testSignatureMiss() {
		__testVoid();
	}

	@JointPoint
	abstract void __testVoid();






}
