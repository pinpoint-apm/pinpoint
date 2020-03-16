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

package com.navercorp.pinpoint.bootstrap.plugin.monitor;

import com.navercorp.pinpoint.common.trace.RequestUrlMappingExtractorType;
import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author Taejin Koo
 */
public class RequestUrlMappingExtractorProvider {

    private RequestUrlMappingExtractorType type;
    private Object parameterValue;

    public RequestUrlMappingExtractorProvider(RequestUrlMappingExtractorType type, Object parameterValue) {
        this.type = Assert.requireNonNull(type, "type");

        if (!type.assertParameter(parameterValue)) {
            throw new IllegalArgumentException("parameterValue has invalid type. (expected type:" + type.getParameterClazzType().getName());
        }
        this.parameterValue = parameterValue;
    }

    public RequestUrlMappingExtractorType getType() {
        return type;
    }

    public Object getParameterValue() {
        return parameterValue;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RequestUrlMappingExtractorProvider{");
        sb.append("type=").append(type);
        sb.append(", parameterValue=").append(parameterValue);
        sb.append('}');
        return sb.toString();
    }
}
