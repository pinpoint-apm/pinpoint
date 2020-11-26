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

package com.navercorp.pinpoint.plugin.common.servlet;

import com.navercorp.pinpoint.bootstrap.plugin.uri.UriExtractor;
import com.navercorp.pinpoint.common.trace.UriExtractorType;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @author Taejin Koo
 */
public class ServletRequestAttributesMappingExtractor implements UriExtractor<HttpServletRequest> {

    static final UriExtractorType TYPE = UriExtractorType.SERVLET_REQUEST_ATTRIBUTE;

    private final String[] attributeNames;

    public ServletRequestAttributesMappingExtractor(String[] attributeNames) {
        if (ArrayUtils.isEmpty(attributeNames)) {
            throw new IllegalArgumentException("attributeNames must not be empty");
        }

        this.attributeNames = attributeNames;
    }

    @Override
    public UriExtractorType getExtractorType() {
        return TYPE;
    }

    @Override
    public String getUri(HttpServletRequest request, String rawUri) {
        for (String attributeName : attributeNames) {
            Object uriMapping = request.getAttribute(attributeName);
            if (!(uriMapping instanceof String)) {
                continue;
            }

            if (StringUtils.hasLength((String) uriMapping)) {
                return (String) uriMapping;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServletRequestAttributesMappingExtractor{");
        sb.append("attributeNames=").append(Arrays.toString(attributeNames));
        sb.append('}');
        return sb.toString();
    }

}
