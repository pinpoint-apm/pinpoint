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

package com.navercorp.pinpoint.bootstrap.logging;

/**
 * @author emeroad
 */
public interface PLogger {


    void beforeInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args);

    void beforeInterceptor(Object target, Object[] args);

    void afterInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result, Throwable throwable);

    void afterInterceptor(Object target, Object[] args, Object result, Throwable throwable);

    void afterInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args);

    void afterInterceptor(Object target, Object[] args);

    boolean isTraceEnabled();


    void trace(String msg);


    void trace(String format, Object arg);



    void trace(String format, Object arg1, Object arg2);

    void trace(String format, Object... argArray);

    void trace(String msg, Throwable t);



    boolean isDebugEnabled();



    void debug(String msg);



    void debug(String format, Object arg);




    void debug(String format, Object arg1, Object arg2);


    void debug(String format, Object... argArray);


    void debug(String msg, Throwable t);





    boolean isInfoEnabled();


    void info(String msg);



    void info(String format, Object arg);



    void info(String format, Object arg1, Object arg2);


    void info(String format, Object... argArray);


    void info(String msg, Throwable t);



    boolean isWarnEnabled();


    void warn(String msg);


    void warn(String format, Object arg);


    void warn(String format, Object... argArray);


    void warn(String format, Object arg1, Object arg2);


    void warn(String msg, Throwable t);



    boolean isErrorEnabled();


    void error(String msg);


    void error(String format, Object arg);


    void error(String format, Object arg1, Object arg2);


    void error(String format, Object... argArray);


    void error(String msg, Throwable t);




}
