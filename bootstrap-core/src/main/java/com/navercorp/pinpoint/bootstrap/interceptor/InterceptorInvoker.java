/**
 * Copyright 2014 NAVER Corp.
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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jongho Moon
 *
 */
public class InterceptorInvoker {
    public static final boolean throwException = "true".equals(System.getProperty("throwExcepton"));
    public static final Logger logger = Logger.getLogger("com.navercorp.pinpoint.bootstrap.interceptor.InterceptorInvoker");
    
    public static SimpleAroundInterceptor before(int interceptorId, Object target, Object[] args) {
        SimpleAroundInterceptor interceptor = InterceptorRegistry.getSimpleInterceptor(interceptorId);

        try {
            interceptor.before(target, args);
        } catch (Throwable t) {
            if (throwException) {
                throw new RuntimeException(t);
            } else {
                logger.log(Level.WARNING, "Excetpion occured from interceptor", t);
            }
        }
        
        return interceptor;
    }
    
    public static SimpleAroundInterceptor after(int interceptorId, Object target, Object[] args, Object result, Throwable throwable) {
        SimpleAroundInterceptor interceptor = InterceptorRegistry.getSimpleInterceptor(interceptorId);
        
        try {
            interceptor.after(target, args, result, throwable);
        } catch (Throwable t) {
            if (throwException) {
                throw new RuntimeException(t);
            } else {
                logger.log(Level.WARNING, "Excetpion occured from interceptor", t);
            }
        }
        
        return interceptor;
    }

    public static void after(SimpleAroundInterceptor interceptor, Object target, Object[] args, Object result, Throwable throwable) {
        try {
            interceptor.after(target, args, result, throwable);
        } catch (Throwable t) {
            if (throwException) {
                throw new RuntimeException(t);
            } else {
                logger.log(Level.WARNING, "Excetpion occured from interceptor", t);
            }
        }
    }

    
}
