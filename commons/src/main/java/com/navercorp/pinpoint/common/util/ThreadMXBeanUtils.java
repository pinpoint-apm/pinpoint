package com.nhn.pinpoint.common.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author emeroad
 */
public final class ThreadMXBeanUtils {

    private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();

    private static final boolean OBJECT_MONITOR_USAGE_SUPPORT;
    private static final boolean SYNCHRONIZER_USAGE_SUPPORT;
    // check support -> getWaitedTime(), getBlockedTime()
    private static final boolean CONTENTION_MONITORING_SUPPORT;

    private ThreadMXBeanUtils() {
    }

    static {
        OBJECT_MONITOR_USAGE_SUPPORT = THREAD_MX_BEAN.isObjectMonitorUsageSupported();
        SYNCHRONIZER_USAGE_SUPPORT =  THREAD_MX_BEAN.isSynchronizerUsageSupported();
        CONTENTION_MONITORING_SUPPORT = THREAD_MX_BEAN.isThreadContentionMonitoringSupported();
        logOption();
    }

    private static void logOption() {
        final Logger logger = Logger.getLogger(ThreadMXBeanUtils.class.getName());
        if (logger.isLoggable(Level.INFO)) {
            final StringBuilder builder = new StringBuilder();
            builder.append("ThreadMXBean SupportOption:{OBJECT_MONITOR_USAGE_SUPPORT=");
            builder.append(OBJECT_MONITOR_USAGE_SUPPORT);
            builder.append("}, {SYNCHRONIZER_USAGE_SUPPORT=");
            builder.append(SYNCHRONIZER_USAGE_SUPPORT);
            builder.append("}, {CONTENTION_MONITORING_SUPPORT=");
            builder.append(CONTENTION_MONITORING_SUPPORT);
            builder.append('}');
            logger.info(builder.toString());
        }
    }

    public static ThreadInfo[] dumpAllThread() {
//        try {
            return THREAD_MX_BEAN.dumpAllThreads(OBJECT_MONITOR_USAGE_SUPPORT, SYNCHRONIZER_USAGE_SUPPORT);
//        ?? handle exception
//        } catch (java.lang.SecurityException se) {
//            log??
//            return new ThreadInfo[]{};
//        } catch (java.lang.UnsupportedOperationException ue) {
//            log??
//            return new ThreadInfo[]{};
//        }
    }

    public static boolean findThreadName(ThreadInfo[] threadInfos, String threadName) {
        if (threadInfos == null) {
            return false;
        }
        for (ThreadInfo threadInfo : threadInfos) {
            if (threadInfo.getThreadName().equals(threadName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean findThreadName(String threadName) {
        final ThreadInfo[] threadInfos = dumpAllThread();
        return findThreadName(threadInfos, threadName);
    }

}
