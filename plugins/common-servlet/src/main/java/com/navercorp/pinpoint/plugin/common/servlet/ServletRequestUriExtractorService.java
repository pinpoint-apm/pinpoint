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
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriExtractorChain;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriExtractorProviderLocator;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriExtractorService;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriMappingExtractorProvider;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class ServletRequestUriExtractorService implements UriExtractorService<HttpServletRequest> {

    @Override
    public UriExtractor<HttpServletRequest> get(UriExtractorProviderLocator uriExtractorProviderLocator) {
        List<UriMappingExtractorProvider> uriMappingExtractorProviderList = uriExtractorProviderLocator.get(UriMappingExtractorProvider.class, ServletRequestAttributesMappingExtractor.TYPE);

        List<UriExtractor<HttpServletRequest>> result = new ArrayList<UriExtractor<HttpServletRequest>>();
        for (UriMappingExtractorProvider uriMappingExtractorProvider : uriMappingExtractorProviderList) {
            ServletRequestAttributesMappingExtractor servletRequestAttributesMappingExtractor = new ServletRequestAttributesMappingExtractor(uriMappingExtractorProvider.getMappingKeyCandidates());
            result.add(servletRequestAttributesMappingExtractor);
        }

        if (result.isEmpty()) {
            return null;
        } else {
            return new UriExtractorChain<HttpServletRequest>(result);
        }
    }

}
