/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.common.trace;

import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author Taejin Koo
 */
public class RequestUrlMappingExtractorTypeFactory {

    public static RequestUrlMappingExtractorType of(String name, Class<?> parameterType) {
        return new DefaultRequestUrlMappingExtractorType(name, parameterType);
    }

    private static class DefaultRequestUrlMappingExtractorType implements RequestUrlMappingExtractorType {

        private final String name;
        private final Class<?> parameterClazzType;

        public DefaultRequestUrlMappingExtractorType(String name, Class<?> parameterClazzType) {
            this.name = Assert.requireNonNull(name, "name");
            this.parameterClazzType = Assert.requireNonNull(parameterClazzType, "parameterClazzType");
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Class<?> getParameterClazzType() {
            return parameterClazzType;
        }

        @Override
        public boolean assertParameter(Object value) {
            if (value == null) {
                return false;
            }

            if (value.getClass() == parameterClazzType) {
                return true;
            }
            return false;
        }

    }

}
