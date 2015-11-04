/*
 * *
 *  * Copyright 2014 NAVER Corp.
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.navercorp.pinpoint.profiler.instrument.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.*;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.IgnoreMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class InterceptorTypeDetector {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final List<TypeHandler> detectHandlers = new ArrayList<TypeHandler>();

    public InterceptorTypeDetector() {
        register();
    }

    public InterceptorDefinition getInterceptorDefinition(Class<?> interceptorClazz) {
        if (interceptorClazz == null) {
            throw new NullPointerException("interceptorClazz must not be null");
        }
        for (TypeHandler typeHandler : detectHandlers) {
            final InterceptorDefinition interceptorDefinition = typeHandler.resolveType(interceptorClazz);
            if (interceptorDefinition != null) {
                return interceptorDefinition;
            }
        }
        throw new RuntimeException("unsupported Interceptor Type. " + interceptorClazz.getName());
    }


    private void register() {

        TypeHandler around = createInterceptorTypeHandler(AroundInterceptor.class, InterceptorType.ARRAY_ARGS);
        add(around);

        TypeHandler around0 = createInterceptorTypeHandler(AroundInterceptor0.class, InterceptorType.BASIC);
        add(around0);

        TypeHandler around1 = createInterceptorTypeHandler(AroundInterceptor1.class, InterceptorType.BASIC);
        add(around1);

        TypeHandler around2 = createInterceptorTypeHandler(AroundInterceptor2.class, InterceptorType.BASIC);
        add(around2);

        TypeHandler around3 = createInterceptorTypeHandler(AroundInterceptor3.class, InterceptorType.BASIC);
        add(around3);

        TypeHandler around4 = createInterceptorTypeHandler(AroundInterceptor4.class, InterceptorType.BASIC);
        add(around4);

        TypeHandler around5 = createInterceptorTypeHandler(AroundInterceptor5.class, InterceptorType.BASIC);
        add(around5);

        TypeHandler staticAround = createInterceptorTypeHandler(StaticAroundInterceptor.class, InterceptorType.STATIC);
        add(staticAround);

        TypeHandler apiIdAwareAroundInterceptor = createInterceptorTypeHandler(ApiIdAwareAroundInterceptor.class, InterceptorType.API_ID_AWARE);
        add(apiIdAwareAroundInterceptor);
    }

    private void add(TypeHandler around) {
        this.detectHandlers.add(around);
    }

    private TypeHandler createInterceptorTypeHandler(Class<? extends Interceptor> interceptorClazz, InterceptorType interceptorType) {
        if (interceptorClazz == null) {
            throw new NullPointerException("interceptorClazz must not be null");
        }
        if (interceptorType == null) {
            throw new NullPointerException("interceptorType must not be null");
        }

        final Method[] declaredMethods = interceptorClazz.getDeclaredMethods();
        if (declaredMethods.length != 2) {
            throw new RuntimeException("invalid Type");
        }
        final String before = "before";
        final Method beforeMethod = findMethodByName(declaredMethods, before);
        final Class<?>[] beforeParamList = beforeMethod.getParameterTypes();

        final String after = "after";
        final Method afterMethod = findMethodByName(declaredMethods, after);
        final Class<?>[] afterParamList = afterMethod.getParameterTypes();

        return new TypeHandler(interceptorClazz, interceptorType, before, beforeParamList, after, afterParamList);
    }


    private Method findMethodByName(Method[] declaredMethods, String methodName) {
        Method findMethod = null;
        for (Method method : declaredMethods) {
            if (method.getName().equals(methodName)) {
                if (findMethod != null) {
                    throw new RuntimeException("duplicated method exist. methodName:" + methodName);
                }
                findMethod = method;
            }
        }
        if (findMethod == null) {
            throw new RuntimeException(methodName + " not found");
        }
        return findMethod;
    }


    private class TypeHandler {
        private final Class<?> interceptorClazz;
        private final InterceptorType interceptorType;
        private final String before;
        private final Class<?>[] beforeParamList;
        private final String after;
        private final Class<?>[] afterParamList;

        public TypeHandler(Class<?> interceptorClazz, InterceptorType interceptorType, String before, final Class<?>[] beforeParamList, final String after, final Class<?>[] afterParamList) {
            if (interceptorClazz == null) {
                throw new NullPointerException("interceptorClazz must not be null");
            }
            if (interceptorType == null) {
                throw new NullPointerException("interceptorType must not be null");
            }
            if (before == null) {
                throw new NullPointerException("before must not be null");
            }
            if (beforeParamList == null) {
                throw new NullPointerException("beforeParamList must not be null");
            }
            if (after == null) {
                throw new NullPointerException("after must not be null");
            }
            if (afterParamList == null) {
                throw new NullPointerException("afterParamList must not be null");
            }
            this.interceptorClazz = interceptorClazz;
            this.interceptorType = interceptorType;
            this.before = before;
            this.beforeParamList = beforeParamList;
            this.after = after;
            this.afterParamList = afterParamList;
        }


        public InterceptorDefinition resolveType(Class<?> targetClazz) {
            if(!this.interceptorClazz.isAssignableFrom(targetClazz)) {
                return null;
            }
            return createInterceptorDefinition(targetClazz);
        }

        private InterceptorDefinition createInterceptorDefinition(Class<?> interceptorClazz) {

            final Method beforeMethod = searchMethod(interceptorClazz, before, beforeParamList);
            if (beforeMethod == null) {
                throw new RuntimeException(before + " method not found. " + Arrays.toString(beforeParamList));
            }
            final boolean beforeIgnoreMethod = beforeMethod.isAnnotationPresent(IgnoreMethod.class);


            final Method afterMethod = searchMethod(interceptorClazz, after, afterParamList);
            if (afterMethod == null) {
                throw new RuntimeException(after + " method not found. " + Arrays.toString(afterParamList));
            }
            final boolean afterIgnoreMethod = afterMethod.isAnnotationPresent(IgnoreMethod.class);


            if (beforeIgnoreMethod == false && afterIgnoreMethod == false) {
                return new InterceptorDefinition(interceptorClazz, interceptorType, CaptureType.AROUND, beforeMethod, afterMethod);
            }
            if (beforeIgnoreMethod == true) {
                return new InterceptorDefinition(interceptorClazz, interceptorType, CaptureType.AFTER, null, afterMethod);
            }
            if (afterIgnoreMethod == true) {
                return new InterceptorDefinition(interceptorClazz, interceptorType, CaptureType.BEFORE, beforeMethod, null);
            }
            return new InterceptorDefinition(interceptorClazz, interceptorType, CaptureType.NON, null, null);
        }

        private Method searchMethod(Class<?> interceptorClazz, String searchMethodName, Class<?>[] searchMethodParameter) {
            if (searchMethodName == null) {
                throw new NullPointerException("methodList must not be null");
            }
//          only DeclaredMethod search ?
//            try {
//                return interceptorClazz.getDeclaredMethod(searchMethodName, searchMethodParameter);
//            } catch (NoSuchMethodException ex) {
//                logger.debug(searchMethodName + " DeclaredMethod not found. search parent class");
//            }
            // search all class
            try {
                return interceptorClazz.getMethod(searchMethodName, searchMethodParameter);
            } catch (NoSuchMethodException ex) {
                logger.debug(searchMethodName +"DeclaredMethod not found.");
            }
            return null;
        }
    }



}
