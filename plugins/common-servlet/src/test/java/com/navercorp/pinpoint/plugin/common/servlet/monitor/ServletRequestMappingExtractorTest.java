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

import com.navercorp.pinpoint.bootstrap.plugin.monitor.RequestUrlMappingExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.RequestUrlMappingExtractorProvider;
import com.navercorp.pinpoint.common.trace.RequestUrlMappingExtractorType;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @author Taejin Koo
 */
public class ServletRequestMappingExtractorTest {

    private final String TEST_ATTRIBUTE_NAME = "test.attribute";

    @Test
    public void getAttributeTest() {
        RequestUrlMappingExtractor<HttpServletRequest> httpServletRequestRequestUrlMappingExtractor = createExtractor(TEST_ATTRIBUTE_NAME);

        HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
        String expectedUrlMapping = "URL_MAPPING";
        Mockito.when(mockHttpServletRequest.getAttribute(TEST_ATTRIBUTE_NAME)).thenReturn(expectedUrlMapping);
        String actualUrlMapping = httpServletRequestRequestUrlMappingExtractor.getUrlMapping(mockHttpServletRequest, "test.html");

        Assert.assertEquals(expectedUrlMapping, actualUrlMapping);
    }

    @Test
    public void notFoundedAttributeTest() {
        RequestUrlMappingExtractor<HttpServletRequest> httpServletRequestRequestUrlMappingExtractor = createExtractor(TEST_ATTRIBUTE_NAME);

        HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
        String actualUrlMapping = httpServletRequestRequestUrlMappingExtractor.getUrlMapping(mockHttpServletRequest, "test.html");

        Assert.assertEquals(RequestUrlMappingExtractor.NOT_FOUNDED_MAPPING, actualUrlMapping);
    }

    @Test
    public void oftenUsedUrlTest() {
        RequestUrlMappingExtractor<HttpServletRequest> httpServletRequestRequestUrlMappingExtractor = createExtractor(TEST_ATTRIBUTE_NAME);

        HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);

        String[] oftenUsedUrls = MultiServletRequestMappingExtractor.DEFAULT_OFTEN_USED_URL;
        for (String oftenUsedUrl : oftenUsedUrls) {
            String actualUrlMapping = httpServletRequestRequestUrlMappingExtractor.getUrlMapping(mockHttpServletRequest, oftenUsedUrl);
            Assert.assertEquals(oftenUsedUrl, actualUrlMapping);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void createFailTest() {
        RequestUrlMappingExtractor<HttpServletRequest> httpServletRequestRequestUrlMappingExtractor = createExtractor(new Object());
    }

    private RequestUrlMappingExtractor<HttpServletRequest> createExtractor(Object parameterValue) {
        ServletRequestAttributeMappingExtractorFactory factory = new ServletRequestAttributeMappingExtractorFactory();

        RequestUrlMappingExtractorProvider provider = new RequestUrlMappingExtractorProvider(RequestUrlMappingExtractorType.SERVLET_REQUEST_ATTRIBUTE, parameterValue);

        return factory.create(Arrays.asList(provider));
    }

}
