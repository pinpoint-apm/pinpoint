package com.navercorp.pinpoint.profiler.interceptor.bci.mock;

import com.navercorp.pinpoint.profiler.interceptor.aspect.Aspect;
import com.navercorp.pinpoint.profiler.interceptor.aspect.JointPoint;
import com.navercorp.pinpoint.profiler.interceptor.aspect.PointCut;

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
