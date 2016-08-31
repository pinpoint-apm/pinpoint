/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
    
    public static List<String> getVmArgs() {
        final List<String> vmArgs = RUNTIME_MBEAN.getInputArguments();
        if (vmArgs == null) {
            return Collections.emptyList();
        }
        return vmArgs;
    }

    private static int getPid0() {
        final String name = RUNTIME_MBEAN.getName();
        final int pidIndex = name.indexOf('@');
        if (pidIndex == -1) {
            getLogger().warn("invalid pid name:" + name);
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
        final int abs = Math.abs(RANDOM.nextInt());
        if (abs == Integer.MIN_VALUE) {
            return -1;
        }
        return abs;
    }

    public static long getVmStartTime() {
        if (START_TIME == 0) {
            START_TIME = getVmStartTime0();
        }
        return START_TIME;
    }

    private static long getVmStartTime0() {
        try {
            return RUNTIME_MBEAN.getStartTime();
        } catch (UnsupportedOperationException e) {
            final Logger logger = getLogger();
            logger.warn("RuntimeMXBean.getStartTime() unsupported. Caused:" + e.getMessage(), e);
            return System.currentTimeMillis();
        }
    }

    public static String getName() {
        return RUNTIME_MBEAN.getName();
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(RuntimeMXBeanUtils.class);
    }

}
