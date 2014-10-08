package com.nhn.pinpoint.profiler.interceptor.bci.mock;

import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.profiler.interceptor.aspect.Aspect;
import com.nhn.pinpoint.profiler.interceptor.aspect.JointPoint;
import com.nhn.pinpoint.profiler.interceptor.aspect.PointCut;

import java.util.Map;

/**
 * @author emeroad
 */
@Aspect
public abstract class TestAspect_NoExtents {

	@PointCut
	public void testVoid() {
		__testVoid();
	}

	@JointPoint
	abstract void __testVoid();



}
