package com.nhn.pinpoint.profiler.util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author emeroad
 */
public final class RuntimeMXBeanUtils {
    private static final RuntimeMXBean RUNTIME_MBEAN = ManagementFactory.getRuntimeMXBean();

    private static long START_TIME = 0;
    private static int PID = 0;
    private static final Random RANDOM = new Random();

    private RuntimeMXBeanUtils() {
    }

    public static int getPid() {
        if (PID == 0) {
            PID = getPid0();
        }
        return PID;
    }

    private static int getPid0() {
        final String name = RUNTIME_MBEAN.getName();
        final int pidIndex = name.indexOf('@');
        if (pidIndex == -1) {
            getLogger().log(Level.WARNING, "invalid pid name:" + name);
            return getNegativeRandomValue();
        }
        String strPid = name.substring(0, pidIndex);
        try {
            return Integer.parseInt(strPid);
        } catch (NumberFormatException e) {
            return getNegativeRandomValue();
        }
    }
    private static int getNegativeRandomValue() {
        return - Math.abs(RANDOM.nextInt());
    }

    public static long getVmStartTime() {
        if (START_TIME == 0) {
            START_TIME = getVmStartTime0();
        }
        return START_TIME;
    }

    private static long getVmStartTime0() {
        try {
            return  RUNTIME_MBEAN.getStartTime();
        } catch (UnsupportedOperationException e) {
            final Logger logger = getLogger();
            logger.log(Level.WARNING, "RuntimeMXBean.getStartTime() unsupported. Caused:" + e.getMessage(), e);
            return System.currentTimeMillis();
        }
    }

    private static Logger getLogger() {
        return Logger.getLogger(RuntimeMXBeanUtils.class.getName());
    }

}
