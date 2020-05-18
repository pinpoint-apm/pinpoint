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

import java.lang.reflect.Method;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Java9PidProvider implements PidProvider {
    private static final PLogger logger = PLoggerFactory.getLogger(Java9PidProvider.class);

    private static final Method pidMethod = getPidMethod();

    private static Method getPidMethod() {
        try {
            return Process.class.getDeclaredMethod("pid");
        } catch (NoSuchMethodException e) {
            logger.warn("Process.pid() not found");
            return null;
        }
    }

    @Override
    public Long getPid(Process process) {
        if (process == null) {
            return null;
        }
        if (pidMethod == null) {
            return null;
        }
        try {
            return (Long) pidMethod.invoke(process);
        } catch (Exception ignore) {
            return null;
        }
    }
}
