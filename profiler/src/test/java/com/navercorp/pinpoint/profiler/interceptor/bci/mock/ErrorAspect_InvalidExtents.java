package com.nhn.pinpoint.profiler.interceptor.bci.mock;

import com.nhn.pinpoint.profiler.interceptor.aspect.Aspect;
import com.nhn.pinpoint.profiler.interceptor.aspect.JointPoint;
import com.nhn.pinpoint.profiler.interceptor.aspect.PointCut;

/**
 * @author emeroad
 */
@Aspect
public abstract class ErrorAspect_InvalidExtents extends Thread {

	@PointCut
	public void testVoid() {
		__testVoid();
	}

	@JointPoint
	abstract void __testVoid();



}
