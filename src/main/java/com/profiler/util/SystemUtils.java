package com.profiler.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class SystemUtils {
    private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();


	public static long getCurrentThreadCpuTime() {
		return THREAD_MX_BEAN.getCurrentThreadCpuTime();
	}

    public static long getCurrentThreadUserTime() {
        return  THREAD_MX_BEAN.getCurrentThreadUserTime();
    }
}
