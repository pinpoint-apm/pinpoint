/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin.shared;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public interface MethodFilter {

    boolean matches(Method method);


    class AndMethodFilter implements MethodFilter {

        private List<MethodFilter> methodFilterList;

        public AndMethodFilter(MethodFilter... methodFilters) {
            this(Arrays.asList(methodFilters));
        }

        public AndMethodFilter(List<MethodFilter> methodFilterList) {
            this.methodFilterList = Objects.requireNonNull(methodFilterList, "methodFilterList");
        }


        @Override
        public boolean matches(Method method) {
            Objects.requireNonNull(method, "method");

            for (MethodFilter methodFilter : methodFilterList) {
                if (!methodFilter.matches(method)) {
                    return false;
                }
            }
            return true;
        }
    }

    class OrMethodFilter implements MethodFilter {
        private List<MethodFilter> methodFilterList;


        OrMethodFilter(MethodFilter... methodFilters) {
            this(Arrays.asList(methodFilters));
        }

        OrMethodFilter(List<MethodFilter> methodFilterList) {
            this.methodFilterList = Objects.requireNonNull(methodFilterList, "methodFilterList");
        }

        @Override
        public boolean matches(Method method) {
            Objects.requireNonNull(method, "method");

            for (MethodFilter methodFilter : methodFilterList) {
                if (methodFilter.matches(method)) {
                    return true;
                }
            }
            return false;
        }
    }

    class AnnotationFilter implements MethodFilter {

        private final Class<? extends Annotation> annotationClazz;

        AnnotationFilter(Class<? extends Annotation> annotationClazz) {
            this.annotationClazz = Objects.requireNonNull(annotationClazz, "annotationClazz");
        }

        @Override
        public boolean matches(Method method) {
            Objects.requireNonNull(method, "method");

            Annotation annotation = method.getAnnotation(annotationClazz);
            return annotation != null;
        }
    }

    class StaticFilter implements MethodFilter {

        @Override
        public boolean matches(Method method) {
            Objects.requireNonNull(method, "method");

            int modifiers = method.getModifiers();
            return Modifier.isStatic(modifiers);
        }
    }

    class NamePrefixFilter implements MethodFilter {

        private final String prefixName;

        NamePrefixFilter(String prefixName) {
            this.prefixName = Objects.requireNonNull(prefixName, "prefixName");
        }

        @Override
        public boolean matches(Method method) {
            Objects.requireNonNull(method, "method");

            return method.getName().startsWith(prefixName);
        }
    }

    class ParameterSizeFilter implements MethodFilter {

        private final int parameterSize;

        public ParameterSizeFilter(int parameterSize) {
            this.parameterSize = parameterSize;
        }

        @Override
        public boolean matches(Method method) {
            Objects.requireNonNull(method, "method");

            int parameterSize = getParameterSize(method);
            return parameterSize == this.parameterSize;
        }

        private int getParameterSize(Method method) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes == null) {
                return 0;
            }
            return parameterTypes.length;
        }

    }

}
