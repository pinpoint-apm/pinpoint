package com.profiler.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class SystemUtils {

	public static long[] getThreadTime() {
		long result[] = new long[2];

		ThreadMXBean bean = ManagementFactory.getThreadMXBean();

		System.out.println(Thread.currentThread().getName() + " CPU:" + bean.getCurrentThreadCpuTime() + " User:" + bean.getCurrentThreadUserTime());

		result[0] = bean.getCurrentThreadCpuTime();
		result[1] = bean.getCurrentThreadUserTime();

		return result;
	}
}
