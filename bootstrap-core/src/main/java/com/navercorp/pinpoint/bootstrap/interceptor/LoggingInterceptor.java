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

package com.navercorp.pinpoint.bootstrap.interceptor;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author emeroad
 */
public class LoggingInterceptor implements StaticAroundInterceptor, SimpleAroundInterceptor {

	private final Logger logger;

	public LoggingInterceptor(String loggerName) {
		this.logger = Logger.getLogger(loggerName);
	}

	@Override
	public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("before " + defaultString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
		}
	}

	@Override
	public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result, Throwable throwable) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("after " + defaultString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result + " Throwable:" + throwable);
		}
	}

    @Override
    public void before(Object target, Object[] args) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("before " + defaultString(target) + " args:" + Arrays.toString(args) );
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("after " + defaultString(target) + " args:" + Arrays.toString(args) + " result:" + result + " Throwable:" + throwable);
        }
    }

    public static String defaultString(final Object object) {
        if (object == null) {
            return "null";
        }
        return object.toString();
    }

}
