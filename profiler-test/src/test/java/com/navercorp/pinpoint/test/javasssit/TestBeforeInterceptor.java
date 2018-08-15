/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.test.javasssit;

import com.navercorp.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author emeroad
 */
public class TestBeforeInterceptor implements StaticAroundInterceptor {
    static {
        System.out.println("load TestBeforeInterceptor cl:" + TestBeforeInterceptor.class.getClassLoader());
    }
    
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public int call = 0;
    public Object target;
    public String className;
    public String methodName;
    public Object[] args;

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        logger.info("BEFORE target:" + target + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
        this.target = target;
        this.className = className;
        this.methodName = methodName;
        this.args = args;
        call++;
        
        TestInterceptors.add(this);
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result, Throwable throwable) {

    }
}
