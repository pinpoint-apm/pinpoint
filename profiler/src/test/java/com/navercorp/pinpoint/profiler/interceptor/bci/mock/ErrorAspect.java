package com.navercorp.pinpoint.profiler.interceptor.bci.mock;

import com.navercorp.pinpoint.profiler.interceptor.aspect.Aspect;
import com.navercorp.pinpoint.profiler.interceptor.aspect.JointPoint;
import com.navercorp.pinpoint.profiler.interceptor.aspect.PointCut;

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
