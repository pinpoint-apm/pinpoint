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

package com.navercorp.pinpoint.plugin.common.servlet.monitor;

import com.navercorp.pinpoint.common.trace.RequestUrlMappingExtractorType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Taejin Koo
 */
class ServletRequestAttributesMappingExtractor implements ServletRequestMappingExtractor {

    private final String attributeName;

    ServletRequestAttributesMappingExtractor(String attributeName) {
        this.attributeName = Assert.requireNonNull(attributeName, "attributeNamesattributeName");
    }

    @Override
    public RequestUrlMappingExtractorType getType() {
        return TYPE;
    }

    @Override
    public String getUrlMapping(HttpServletRequest request, String rawUrl) {
        Object urlMapping = request.getAttribute(attributeName);
        if (urlMapping instanceof String && StringUtils.hasLength((String) urlMapping)) {
            return (String) urlMapping;
        }

        return null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServletRequestAttributesMappingExtractor{");
        sb.append("attributeName='").append(attributeName).append('\'');
        sb.append('}');
        return sb.toString();
    }

}