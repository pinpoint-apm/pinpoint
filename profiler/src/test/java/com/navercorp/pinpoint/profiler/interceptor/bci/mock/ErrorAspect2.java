package com.nhn.pinpoint.profiler.interceptor.bci.mock;

import com.nhn.pinpoint.profiler.interceptor.aspect.Aspect;
import com.nhn.pinpoint.profiler.interceptor.aspect.JointPoint;
import com.nhn.pinpoint.profiler.interceptor.aspect.PointCut;

/**
 * @author emeroad
 */
@Aspect
public abstract class ErrorAspect2 {

	@PointCut
	public int testInternalTypeMiss() {
		__testVoid();
		return 0;
	}

	@JointPoint
	abstract void __testVoid();






}
