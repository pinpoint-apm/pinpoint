package com.navercorp.pinpoint.profiler.interceptor.bci.mock;

import com.navercorp.pinpoint.profiler.interceptor.aspect.Aspect;
import com.navercorp.pinpoint.profiler.interceptor.aspect.JointPoint;
import com.navercorp.pinpoint.profiler.interceptor.aspect.PointCut;

/**
 * @author emeroad
 */
@Aspect
public abstract class TestAspect_ExtentsSub extends OriginalSub {

	@PointCut
	public void testVoid() {
		touchBefore();
		__testVoid();
		touchAfter();
	}

	@JointPoint
	abstract void __testVoid();



}
