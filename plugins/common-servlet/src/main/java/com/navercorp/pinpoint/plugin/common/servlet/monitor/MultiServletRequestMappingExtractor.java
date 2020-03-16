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

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
class MultiServletRequestMappingExtractor implements ServletRequestMappingExtractor {

    private List<ServletRequestMappingExtractor> servletRequestMappingExtractorList;

    MultiServletRequestMappingExtractor(List<ServletRequestMappingExtractor> servletRequestMappingExtractorList) {
        Assert.requireNonNull(servletRequestMappingExtractorList, "servletRequestMappingExtractorList");
        this.servletRequestMappingExtractorList = new ArrayList<ServletRequestMappingExtractor>(servletRequestMappingExtractorList);
    }

    @Override
    public RequestUrlMappingExtractorType getType() {
        return TYPE;
    }

    @Override
    public String getUrlMapping(HttpServletRequest target, String rawUrl) {
        for (ServletRequestMappingExtractor extractor : servletRequestMappingExtractorList) {
            String url = extractor.getUrlMapping(target, null);
            if (url != null) {
                return url;
            }
        }

        for (String oftenUsedUrl : DEFAULT_OFTEN_USED_URL) {
            if (oftenUsedUrl.equals(rawUrl)) {
                return oftenUsedUrl;
            }
        }

        return NOT_FOUNDED_MAPPING;
    }

}

