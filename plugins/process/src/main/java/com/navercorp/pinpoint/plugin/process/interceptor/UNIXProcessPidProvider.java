/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.process.interceptor;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.annotations.VisibleForTesting;

import java.lang.reflect.Field;

/**
 *  
 * @author Woonduk Kang(emeroad)
 */
public class UNIXProcessPidProvider implements PidProvider {
    private static final PLogger logger = PLoggerFactory.getLogger(Java9PidProvider.class);

    // for test
    static final String PROCESS_CLASS_NAME = "java.lang.UNIXProcess";
    static final String PID_FIELD_NAME = "pid";
    private static final Field pidField = getPidField();

    public UNIXProcessPidProvider() {
    }

    private static Field getPidField() {
        try {
            Class<?> unixProcess = getUnixProcess();
            Field pid = getPidField(unixProcess);
            return pid;
        } catch (ClassNotFoundException cnf) {
            logger.info(PROCESS_CLASS_NAME + " not found");
            return null;
        } catch (NoSuchFieldException noSuchFieldException) {
            logger.warn(PID_FIELD_NAME + " not found", noSuchFieldException);
            return null;
        }

    }

    @VisibleForTesting
    static Field getPidField(Class<?> unixProcess) throws NoSuchFieldException {
        Field pid = unixProcess.getDeclaredField(PID_FIELD_NAME);
        pid.setAccessible(true);
        return pid;
    }

    @VisibleForTesting
    static Class<?> getUnixProcess() throws ClassNotFoundException {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        return systemClassLoader.loadClass(PROCESS_CLASS_NAME);
    }

    @Override
    public Long getPid(Process process) {
        if (process == null) {
            return null;
        }
        if (pidField == null) {
            return null;
        }
        try {
            return pidField.getLong(process);
        } catch (Exception ignore) {
            return null;
        }
    }
}
