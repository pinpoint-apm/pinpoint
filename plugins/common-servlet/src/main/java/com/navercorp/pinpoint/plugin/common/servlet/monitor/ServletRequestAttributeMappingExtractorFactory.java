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

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.RequestUrlMappingExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.RequestUrlMappingExtractorFactory;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.RequestUrlMappingExtractorProvider;
import com.navercorp.pinpoint.common.trace.RequestUrlMappingExtractorType;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class ServletRequestAttributeMappingExtractorFactory implements RequestUrlMappingExtractorFactory<HttpServletRequest> {

    @Override
    public RequestUrlMappingExtractor<HttpServletRequest> create(List<RequestUrlMappingExtractorProvider> providerList) {
        List<ServletRequestMappingExtractor> result = new ArrayList<ServletRequestMappingExtractor>();
        for (RequestUrlMappingExtractorProvider provider : providerList) {
            ServletRequestMappingExtractor servletRequestUrlMappingExtractor = create(provider);
            if (servletRequestUrlMappingExtractor != null) {
                result.add(servletRequestUrlMappingExtractor);
            }
        }

        if (result.size() == 0) {
            return null;
        }

        return new MultiServletRequestMappingExtractor(result);
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    private static ServletRequestMappingExtractor create(RequestUrlMappingExtractorProvider provider) {
        RequestUrlMappingExtractorType type = provider.getType();
        if (type != ServletRequestMappingExtractor.TYPE) {
            return null;
        }

        Object parameterValue = provider.getParameterValue();
        if (!type.assertParameter(parameterValue)) {
            throw new IllegalArgumentException("parameterValue has invalid type. (expected type:" + type.getParameterClazzType().getName() + ")");
        }

        String parameter = (String) parameterValue;
        return new ServletRequestAttributesMappingExtractor(parameter);
    }


}
